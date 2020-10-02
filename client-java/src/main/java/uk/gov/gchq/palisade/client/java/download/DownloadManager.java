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
package uk.gov.gchq.palisade.client.java.download;

import org.immutables.value.Value;
import org.slf4j.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import uk.gov.gchq.palisade.client.java.data.DataClient;
import uk.gov.gchq.palisade.client.java.resource.*;
import uk.gov.gchq.palisade.client.java.util.ImmutableStyle;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import com.google.common.eventbus.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DownloadManager {

    @Value.Immutable
    @ImmutableStyle
    public interface IDownloadManagerConfig {
        String getId();
        EventBus getEventBus();

        @Value.Default
        default int getNumThreads() {
            return 1;
        }
    }

    public static DownloadManager createDownloadManager(UnaryOperator<DownloadManagerConfig.Builder> func) {
        var config = func.apply(DownloadManagerConfig.builder()).build();
        return new DownloadManager(config);
    }

    private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);

    private final ThreadPoolExecutor executor;
    private final LinkedBlockingQueue<Runnable> queue;
    private final DownloadManagerConfig config;
    private final AtomicInteger activeThreads;
    private final int numThreads;

    private int numOK = 0;
    private int numFail = 0;

    private final DownloadTracker downloadTracker = new DownloadTracker() {
        @Override
        public int getAvaliableSlots() {
            return numThreads - activeThreads.intValue();
        }
        @Override
        public boolean hasAvailableSlots() {
            return getAvaliableSlots() > 0;
        }
        @Override
        public ManagerStatus getStatus() {
            if (executor.isTerminating()) {
                return ManagerStatus.SHUTTING_DOWN;
            } else if (executor.isShutdown()) {
                return ManagerStatus.SHUT_DOWN;
            }
            return ManagerStatus.ACTIVE;
        }
        @Override
        public int getNumSuccessful() {
            return numOK;
        }
        @Override
        public int getNumFailed() {
            return numFail;
        }
    };

    private DownloadManager(DownloadManagerConfig config) {
        this.config = config;
        this.numThreads = config.getNumThreads();
        this.queue = new LinkedBlockingQueue<>(2);
        this.executor = new ThreadPoolExecutor(numThreads, numThreads, 0L, MILLISECONDS, this.queue);
        this.activeThreads = new AtomicInteger();
        log.debug("Download manager created with thread pool size of {}", numThreads);
    }

    public void schedule(Resource resource) {

        var eventBus = config.getEventBus();
        var token = resource.getToken();
        var url = resource.getUrl();

        log.debug("Scheduling resource: {}", resource);

        // create a connection to data service
        var dataClient = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl(url)
                .build()
                .create(DataClient.class);

        var downloader = Downloader.create(b -> b
                .resource(resource)
                .eventBus(eventBus)
                .dataClient(dataClient));

        executor.execute(() -> {
            var at = activeThreads.addAndGet(1);
            log.debug("Number of actice threads = {}", at);
            eventBus.post(DownloadStartedEvent.of(token));
            try {
                downloader.run();
                numOK++;
                eventBus.post(DownloadCompletedEvent.of(token));
            } catch (Exception t) {
                numFail++;
                eventBus.post(DownloadFailedEvent.of(token, t));
            } finally {
                at = activeThreads.decrementAndGet();
                log.debug("Number of actice threads = {}", at);
            }
        });

    }

    public String getId() {
        return config.getId();
    }

    public DownloadTracker getDownloadTracker() {
        return this.downloadTracker;
    }

    @Subscribe
    public void onJobComplete(ResourcesExhaustedEvent event) {

        // we need to shut the executor down

        executor.shutdown();

    }


}