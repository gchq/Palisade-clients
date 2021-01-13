/*
 * Copyright 2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.client.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.NoSubscriberEvent;
import org.greenrobot.eventbus.Subscribe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.download.DownloadEvent.CompletedEvent;
import uk.gov.gchq.palisade.client.download.DownloadEvent.FailedEvent;
import uk.gov.gchq.palisade.client.download.DownloadEvent.ScheduledEvent;
import uk.gov.gchq.palisade.client.download.DownloadEvent.StartedEvent;
import uk.gov.gchq.palisade.client.receiver.FileReceiver;
import uk.gov.gchq.palisade.client.resource.ResourceMessage;
import uk.gov.gchq.palisade.client.util.Configuration;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static uk.gov.gchq.palisade.client.download.DownloadManager.createDownloadManager;

/**
 * This class must be public for the event bus to work. SonarQube complains
 * about this. The subscribe methods have to be public also.
 */
@MicronautTest
@Property(name = "palisade.client.url", value = DownloadManagerTest.BASE_URL)
@Property(name = "micronaut.server.port", value = DownloadManagerTest.PORT)
public class DownloadManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadManagerTest.class);

    static final String PORT = "8083";
    static final String BASE_URL = "http://localhost:8083";

    private static final String TOKEN = "abcd-1";

    private static ObjectMapper objectMapper;
    private static EventBus eventBus;
    private static Configuration configuration;

    private DownloadManager downloadManager;
    private List<Object> events;

    @Inject
    EmbeddedServer embeddedServer;

    @BeforeAll
    static void setupAll() {
        objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        eventBus = EventBus.getDefault();
        configuration = Configuration.fromDefaults();
    }

    @BeforeEach
    void setup() {
        LOGGER.debug("setup");
        eventBus.register(this);
        this.events = new ArrayList<>();
        this.downloadManager =
            createDownloadManager(b -> b
                .numThreads(1)
                .objectMapper(objectMapper));
    }

    @AfterEach
    void teardown() {
        LOGGER.debug("tearDown");
        downloadManager.shutdown();
        try {
            var success = downloadManager.awaitTermination(10, TimeUnit.SECONDS);
            if (!success) {
                downloadManager.shutdownNow();
            }
        } catch (InterruptedException e) {
            downloadManager.shutdownNow();
        }
        // we do not unregister until this instance is unregistered
        eventBus.unregister(this);
    }

    @Test
    void testSuccessfulDownload() {

        var resource = ResourceMessage.createResource(r -> r
            .leafResourceId("resources/pi0.txt")
            .token(TOKEN)
            .url(BASE_URL));

        var downloadId = downloadManager.schedule(
            resource,
            eventBus,
            new FileReceiver(),
            configuration);

        await().atMost(ofSeconds(5)).until(() -> events.size() == 3);
        assertThat(events).hasSize(3);

        // the first event caught should be a download started

        var eventObject = events.get(0);
        assertThat(eventObject).isInstanceOf(ScheduledEvent.class);
        var scheduledEvent = (ScheduledEvent) eventObject;
        assertThat(scheduledEvent.getId()).isEqualTo(downloadId);
        assertThat(scheduledEvent.getResource()).isEqualTo(resource);

        eventObject = events.get(1);
        assertThat(eventObject).isInstanceOf(StartedEvent.class);
        var startedEvent = (StartedEvent) eventObject;
        assertThat(startedEvent.getId()).isEqualTo(downloadId);
        assertThat(startedEvent.getResource()).isEqualTo(resource);

        // the second event caught should be a download completed

        eventObject = events.get(2);
        assertThat(eventObject).isInstanceOf(CompletedEvent.class);
        var completedEvent = (CompletedEvent) eventObject;
        assertThat(completedEvent.getId()).isEqualTo(downloadId);
        assertThat(completedEvent.getResource()).isEqualTo(resource);

    }

    @Test
    void testFailedMissingDownload() {

        var resourceName = "i do not exist";

        var resource = ResourceMessage.createResource(r -> r
            .leafResourceId(resourceName)
            .token(TOKEN)
            .url(BASE_URL));

        var downloadId = downloadManager.schedule(
            resource,
            eventBus,
            new FileReceiver(),
            configuration);

        await().atMost(ofSeconds(5)).until(() -> events.size() == 3);
        assertThat(events).hasSize(3);

        // the first event caught should be a download scheduled

        var eventObject = events.get(0);
        assertThat(eventObject).isInstanceOf(ScheduledEvent.class);
        var scheduledEvent = (ScheduledEvent) eventObject;
        assertThat(scheduledEvent.getId()).isEqualTo(downloadId);
        assertThat(scheduledEvent.getResource()).isEqualTo(resource);

        // the second event caught should be a download started

        eventObject = events.get(1);
        assertThat(eventObject).isInstanceOf(StartedEvent.class);
        var startedEvent = (StartedEvent) eventObject;
        assertThat(startedEvent.getId()).isEqualTo(downloadId);
        assertThat(startedEvent.getResource()).isEqualTo(resource);

        // the second event caught should be a download completed

        eventObject = events.get(2);
        assertThat(eventObject).isInstanceOf(FailedEvent.class);
        var failedEvent = (FailedEvent) eventObject;
        assertThat(failedEvent.getId()).isEqualTo(downloadId);
        assertThat(failedEvent.getResource()).isEqualTo(resource);
        assertThat(failedEvent.getCause()).isInstanceOf(DownloaderException.class);
        var exception = (DownloaderException) failedEvent.getCause();
        assertThat(exception.getStatusCode()).isEqualTo(404);
        assertThat(exception).hasMessage("Resource \"" + resourceName + "\" not found").hasNoCause();

    }

    /**
     * Handles the provided event. Any method annotated with {@code Subscribe} must
     * be public and be in a public class. This is a requirement of the
     * {@code EventBus}
     *
     * @param e the event
     */
    @Subscribe
    public void handleStarted(final StartedEvent e) {
        this.events.add(e);
    }

    /**
     * Handles the provided event. Any method annotated with {@code Subscribe} must
     * be public and be in a public class. This is a requirement of the
     * {@code EventBus}
     *
     * @param e the event
     */
    @Subscribe
    public void handleCompleted(final CompletedEvent e) {
        this.events.add(e);
    }

    /**
     * Handles the provided event. Any method annotated with {@code Subscribe} must
     * be public and be in a public class. This is a requirement of the
     * {@code EventBus}
     *
     * @param e the event
     */
    @Subscribe
    public void handleFailed(final FailedEvent e) {
        this.events.add(e);
    }

    /**
     * Handles the provided event. Any method annotated with {@code Subscribe} must
     * be public and be in a public class. This is a requirement of the
     * {@code EventBus}
     *
     * @param e the event
     */
    @Subscribe
    public void handleScheduled(final ScheduledEvent e) {
        this.events.add(e);
    }

    /**
     * Handles the provided event. Any method annotated with {@code Subscribe} must
     * be public and be in a public class. This is a requirement of the
     * {@code EventBus}
     *
     * @param e the event
     */
    @Subscribe
    public void handleNoSubscriber(final NoSubscriberEvent e) {
        this.events.add(e);
    }

}
