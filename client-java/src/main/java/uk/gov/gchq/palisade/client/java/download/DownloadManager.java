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

import io.reactivex.*;
import org.reactivestreams.Publisher;
import org.slf4j.*;

import uk.gov.gchq.palisade.client.java.ClientConfig;
import uk.gov.gchq.palisade.client.java.resource.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import com.google.common.eventbus.Subscribe;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DownloadManager {

    public static DownloadManager createDownloadManager(UnaryOperator<DownloadManagerConfig.Builder> func) {
        var config = func.apply(DownloadManagerConfig.builder()).build();
        return new DownloadManager(config);
    }

    private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);

    private final ThreadPoolExecutor executor;
    private final LinkedBlockingQueue<Download> downloads;
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
        this.numThreads = config.getApplicationContext().getBean(ClientConfig.class).getDownload().getThreads();
        this.executor = new ThreadPoolExecutor(numThreads, numThreads, 0L, MILLISECONDS, new LinkedBlockingQueue<>(2));
        this.activeThreads = new AtomicInteger();
        this.downloads = new LinkedBlockingQueue<>(numThreads);
        log.debug("Download manager created with thread pool size of {}", numThreads);
    }

    public void schedule(Resource resource) {

        var eventBus = config.getEventBus();
        var token = resource.getToken();
        var url = resource.getUrl();

        log.debug("Scheduling resource: {}", resource);

        executor.execute(() -> {
            var at = activeThreads.addAndGet(1);
            log.debug("Number of actice threads = {}", at);
            eventBus.post(DownloadStartedEvent.of(token));
            Downloader.create(b -> b.resource(resource).eventBus(eventBus)).run();
            numOK++;
            eventBus.post(DownloadCompletedEvent.of(token));
        });

    }

    public String getId() {
        return config.getId();
    }

    public DownloadTracker getDownloadTracker() {
        return this.downloadTracker;
    }

    @Subscribe
    public void newStream(DownloadReadyEvent event) {
        downloads.offer(Download
                .builder()
                .token(event.getToken())
                .stream(event.getInputStream())
                .resource(event.getResource())
                .build());
        log.debug("### New stream added to queue");
    }

    @Subscribe
    public void onJobComplete(ResourcesExhaustedEvent event) {
        // add a Download with no input stream to the queue. This will instruct the
        // stream to terminate
        downloads.add(Download.builder().token("end").build());
        executor.shutdown();
    }

    public Publisher<Download> subscribe() {
        return Flowable.create(new FlowableOnSubscribe<Download>() {
            @Override
            public void subscribe(FlowableEmitter<Download> emitter) throws Exception {
                var complete = false;
                while (!complete) {
                    var download = downloads.take();
                    if (download.getStream().isEmpty()) {
                        log.debug("### Flowable complete");
                        emitter.onComplete();
                        complete = true;
                    } else {
                        try {
                            log.debug("### Flowable onNext");
                            emitter.onNext(download);
                        } catch (Exception e) {
                            log.debug("### Flowable onError");
                            emitter.onError(e);
                        }
                    }
                }
            }
        }, BackpressureStrategy.ERROR);
    }

}