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
package uk.gov.gchq.palisade.client.receiver;

import uk.gov.gchq.palisade.client.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A receiver that saves an input stream to a file
 *
 * @since 0.5.0
 */
public class FileReceiver implements Receiver {

    /**
     * Property key for the number of bytes downloaded
     */
    public static final String BYTES_KEY = "bytes";

    /**
     * Property key for the path where downloads are saved
     */
    public static final String PATH_KEY = "path";

    /**
     * Property key for the filename
     */
    public static final String FILENAME_KEY = "filename";

    private static final String FILE_PATH_KEY = "receiver.file.path";

    /**
     * Create the new instance
     */
    public FileReceiver() {
        super();
    }

    @Override
    public IReceiverResult process(final ReceiverContext ctx, final InputStream is) throws ReceiverException {

        var resource = ctx.getResource();
        var token = resource.getToken();
        var resourceId = resource.getLeafResourceId();

        String pathTemplate = (String) ctx.getProperty(FILE_PATH_KEY);

        var replacementMap = Map.<String, Supplier<String>>of(
            "%t", () -> token,
            "%s", () -> Util.timeStampFormat(Instant.now()),
            "%r", () -> resourceId.replace('/', '_').replace('\\', '_'));

        var pathString = Util.replaceTokens(pathTemplate, replacementMap);
        var path = Path.of(pathString);

        try (is) {

            Files.createDirectories(path.getParent());

            var len = Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);

            return () -> Map.of(
                PATH_KEY, path.toAbsolutePath().toString(),
                BYTES_KEY, "" + len,
                FILENAME_KEY, path.getFileName().toString());

        } catch (IOException e) {
            throw new ReceiverException("Failed to write downloaded resource to " + path, e);
        }

    }

}
