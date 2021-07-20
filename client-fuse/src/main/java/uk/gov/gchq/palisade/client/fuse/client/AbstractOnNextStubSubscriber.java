/*
 * Copyright 2018-2021 Crown Copyright
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

package uk.gov.gchq.palisade.client.fuse.client;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Consumer;

/**
 * Stub the Subscriber interface such that it only requires an {@link Subscriber#onNext(Object)}
 * method.
 *
 * @param <T> type of elements emitted by the subscriber
 */
public abstract class AbstractOnNextStubSubscriber<T> implements Subscriber<T> {
    Subscription subscription;

    @Override
    public void onSubscribe(final Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(final T t) {
        this.subscription.request(1);
    }

    @Override
    public void onError(final Throwable throwable) {
    }

    @Override
    public void onComplete() {
    }

    static <T> AbstractOnNextStubSubscriber<T> fromOnNextMethod(final Consumer<T> onNext) {
        return new AbstractOnNextStubSubscriber<T>() {
            @Override
            public void onNext(final T t) {
                onNext.accept(t);
                super.onNext(t);
            }
        };
    }
}
