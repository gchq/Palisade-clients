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

package uk.gov.gchq.palisade.client.s3.config;

import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.MediaType;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JacksonXmlSupport {
    private static final List<MediaType> XML_MEDIA_TYPES = Arrays.asList(MediaTypes.APPLICATION_XML, MediaTypes.TEXT_XML);
    private static final ObjectMapper DEFAULT_XML_MAPPER = new XmlMapper()
            .enable(SerializationFeature.WRAP_ROOT_VALUE);

    public static <T> Marshaller<T, RequestEntity> marshaller() {
        return Marshaller.wrapEntity(
                JacksonXmlSupport::toXML,
                Marshaller.stringToEntity(),
                MediaTypes.APPLICATION_XML
        );
    }

    public static <T> Unmarshaller<HttpEntity, T> unmarshaller(final Class<T> expectedType) {
        return Unmarshaller.forMediaTypes(XML_MEDIA_TYPES, Unmarshaller.entityToString())
                .thenApply(xml -> fromXML(xml, expectedType));
    }

    private static <T> String toXML(final T object) {
        try {
            return JacksonXmlSupport.DEFAULT_XML_MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot marshal to XML: " + object, e);
        }
    }

    private static <T> T fromXML(final String xml, final Class<T> expectedType) {
        try {
            return JacksonXmlSupport.DEFAULT_XML_MAPPER.readerFor(expectedType).readValue(xml);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot unmarshal XML as " + expectedType.getSimpleName(), e);
        }
    }
}

