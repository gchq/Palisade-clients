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

package uk.gov.gchq.palisade.client.shell;

import java.util.Optional;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Consumer;

public class DelegatingSubscriber<T> implements Subscriber<T> {
    Consumer<Subscription> onSubscribe;
    Consumer<T> onNext;
    Consumer<Throwable> onError;
    Runnable onComplete;

    public DelegatingSubscriber(final Consumer<Subscription> onSubscribe, final Consumer<T> onNext, final Consumer<Throwable> onError, final Runnable onComplete) {
        this.onSubscribe = onSubscribe;
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        Optional.ofNullable(onSubscribe).ifPresent(consumer -> consumer.accept(subscription));
    }

    @Override
    public void onNext(final T t) {
        Optional.ofNullable(onNext).ifPresent(consumer -> consumer.accept(t));
    }

    @Override
    public void onError(final Throwable throwable) {
        Optional.ofNullable(onError).ifPresent(consumer -> consumer.accept(throwable));
    }

    @Override
    public void onComplete() {
        Optional.ofNullable(onComplete).ifPresent(Runnable::run);
    }
}
