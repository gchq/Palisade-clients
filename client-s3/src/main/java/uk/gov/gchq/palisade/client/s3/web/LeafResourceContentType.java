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

package uk.gov.gchq.palisade.client.s3.web;

import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.MediaType;
import akka.http.javadsl.model.MediaTypes;

import uk.gov.gchq.palisade.resource.LeafResource;

import java.util.Arrays;

class LeafResourceContentType {
    public static MediaType.Binary mediaType(final String mediaTypeStr) {
        String[] parts = mediaTypeStr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Parts for format '" + mediaTypeStr + "' was now two: " + Arrays.toString(parts));
        }
        String mainType = parts[0];
        String subType = parts[1];

        return MediaTypes.customBinary(mainType, subType, false);
    }

    public static ContentType contentType(final String contentTypeStr) {
        String[] parts = contentTypeStr.split(";");

        if (parts.length != 1 && parts.length != 2) {
            throw new IllegalArgumentException("Parts for format '" + contentTypeStr + "' was not one or two: " + Arrays.toString(parts));
        }
        String mediaTypeStr = parts[0];
        String charset;
        if (parts.length == 2) {
            charset = parts[1];
        } else {
            charset = null;
        }
        return ContentTypes.create(mediaType(mediaTypeStr));
    }

    public static ContentType create(final LeafResource leafResource) {
        return contentType(leafResource.getSerialisedFormat());
    }
}
