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
package uk.gov.gchq.palisade.client.receiver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * A receiver that saves an input stream to a file
 *
 * @since 0.5.0
 */
public class FileReceiver implements Receiver {

    private static final String SEPARATOR = File.separator;

    // e.g. my/path/to/pal_token_resource_
    private static final String FILE_TEMPLATE = "%s" + SEPARATOR + "pal-%s-%s";
    private static final String FILE_PATH_KEY = "receiver.file.path";

    @Override
    public IReceiverResult process(final ReceiverContext ctx, final InputStream is) throws ReceiverException {

        var resource = ctx.getResource();
        var token = resource.getToken();
        var resourceId = resource.getLeafResourceId();

        String pathString = (String) ctx.getProperty(FILE_PATH_KEY);

        if (pathString.endsWith(SEPARATOR)) {
            pathString = pathString.substring(0, pathString.length() - 1);
        }

        var outFilename = String.format(FILE_TEMPLATE, pathString, token,
            resourceId.replace('/', '_').replace('\\', '_'));

        try (is) {

            Path file = Path.of(outFilename);
            var len = Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);

            return () -> Map.of(
                IReceiverResult.PATH_KEY, file.toAbsolutePath().toString(),
                IReceiverResult.BYTES_KEY, "" + len,
                IReceiverResult.FILENAME_KEY, file.getFileName().toString());

        } catch (IOException e) {
            throw new ReceiverException("Failed to write downloaded resource to " + outFilename, e);
        }

    }

}
