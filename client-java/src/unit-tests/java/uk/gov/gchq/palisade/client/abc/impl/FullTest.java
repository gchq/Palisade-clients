package uk.gov.gchq.palisade.client.abc.impl;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.client.abc.ClientManager;
import uk.gov.gchq.palisade.client.abc.Message;

import javax.inject.Inject;

import java.util.Map;

/**
 * @since 0.5.0
 */
@MicronautTest
class FullTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FullTest.class);

    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void test() throws Exception {

        var properties = Map.<String, String>of(
            "service.palisade.port", "" + embeddedServer.getPort(),
            "service.filteredResource.port", "" + embeddedServer.getPort());

        var session = ClientManager.openSession("pal://mrblobby@localhost/cluster", properties);
        var query = session.createQuery("resource_id");
        var queryResponse = query.execute();
        var stream = queryResponse.stream();

        var flowable = Flowable.fromPublisher(FlowAdapters.toPublisher(stream));

        flowable.subscribe(new FlowableSubscriber<Message>() {

            @Override
            public void onNext(final Message t) {
                LOGGER.debug("## Got message: {}", t);
                System.out.println("## Got message: " + t);
            }

            @Override
            public void onError(final Throwable t) {
                LOGGER.debug("## Error: {}", t.getMessage());
            }

            @Override
            public void onComplete() {
                LOGGER.debug("## complete");

            }

            @Override
            public void onSubscribe(final org.reactivestreams.@NonNull Subscription s) {
                LOGGER.debug("## Subscribed");
            }
        });

//        stream.subscribe(new Subscriber<Message>() {
//
//            @Override
//            public void onSubscribe(final Subscription s) {
//                LOGGER.debug("## Subscribed");
//
//            }
//
//            @Override
//            public void onNext(final Message t) {
//                LOGGER.debug("## Got message: {}", t);
//                System.out.println("## Got message: " + t);
//            }
//
//            @Override
//            public void onError(final Throwable t) {
//                LOGGER.debug("## Error: {}", t.getMessage());
//
//            }
//
//            @Override
//            public void onComplete() {
//                LOGGER.debug("## complete");
//            }
//        });

        Thread.sleep(2000);
    }

}
