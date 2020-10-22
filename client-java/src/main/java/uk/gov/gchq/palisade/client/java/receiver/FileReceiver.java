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

import uk.gov.gchq.palisade.client.java.resource.Resource;

import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A receiver that saves an input stream to a file
 *
 * @since 0.5.0
 */
@Singleton
public class FileReceiver implements Receiver {

    @Override
    public void process(final ReceiverContext receiverContext, final InputStream inputStream) throws ReceiverException {
        Resource resource = receiverContext.getResource();
        String token = resource.getToken();
        String resourceId = resource.getLeafResourceId();
        String outFilename = "/tmp/pal-" + token + "-" + resourceId;

        try {
            write(inputStream, outFilename);
        } catch (IOException e) {
            throw new ReceiverException(resource, "Failed to write downloaded resource to " + outFilename, e);
        }
    }

    private static void write(final InputStream initialStream, final String filename) throws IOException {
        try (initialStream) {
            File file = new File(filename);
            Files.copy(initialStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}