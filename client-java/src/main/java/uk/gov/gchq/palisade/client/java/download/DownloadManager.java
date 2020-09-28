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

    private DownloadTracker downloadTracker = new DownloadTracker() {
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
    };

    private DownloadManager(DownloadManagerConfig config) {
        this.config = config;
        this.numThreads = config.getNumThreads();
        this.queue = new LinkedBlockingQueue<Runnable>(2);
        this.executor = new ThreadPoolExecutor(numThreads, numThreads, 0L, MILLISECONDS, this.queue);
        this.activeThreads = new AtomicInteger();
        log.debug("Download manager created with thread pool size of {}", numThreads);
    }

    @Subscribe
    public void schedule(ResourceReadyEvent event) {

        var bus = config.getEventBus();
        var token = event.getToken();
        var resource = event.getResource();
        var downloader = Downloader.create(b -> b.token(token).resource(resource));

        executor.execute(() -> {
            var at = activeThreads.addAndGet(1);
            log.debug("Number of actice threads = " + at);
            bus.post(DownloadStartedEvent.of(token));
            try {
                downloader.run();
                bus.post(DownloadCompletedEvent.of(token));
            } catch (Exception t) {
                bus.post(DownloadFailedEvent.of(token, t));
            } finally {
                at = activeThreads.decrementAndGet();
                log.debug("Number of actice threads = " + at);
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