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
package uk.gov.gchq.palisade.client.java.receiver;

import org.slf4j.*;

import javax.inject.Singleton;

import java.io.InputStream;

/**
 * A receiver that saves an input stream to a file
 *
 * @author dbell
 * @since 0.5.0
 */
@Singleton
public class LoggingReceiver implements Receiver {

    private static final Logger log = LoggerFactory.getLogger(LoggingReceiver.class);

    /**
     * Create a new file reviver
     */
    public LoggingReceiver() { // empty
    }

    @Override
    public void process(ReceiverContext receiverContext, InputStream is) {

        log.debug("Received: {}", receiverContext);

    }


  }