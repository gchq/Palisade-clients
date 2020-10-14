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
package uk.gov.gchq.palisade.client.java.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * <p>
 * This class is the base class of more concrete implementations to
 * serialise/deserialise objects to and from text streams. This class is used by
 * the web socket to translate strings to/from objects.
 * </p>
 * <p>
 * When {@code #init(EndpointConfig)} is called, the type generic type is
 * retrieved from the superclass
 * </p>
 *
 * @param <T> The type of object to be serialised to and deserialised from
 * @since 0.5.0
 */
public abstract class JSONCoder<T> implements Encoder.TextStream<T>, Decoder.TextStream<T> {

    /**
     * The type
     */
    private Class<T> type;

    /**
     * Store the object mapper in each thread
     */
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new Jdk8Module());

    @SuppressWarnings("unchecked")
    @Override
    public void init(final EndpointConfig endpointConfig) {
        ParameterizedType thisClass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type typeT = thisClass.getActualTypeArguments()[0];
        if (typeT instanceof Class) {
            type = (Class<T>) typeT;
        } else if (typeT instanceof ParameterizedType) {
            type = (Class<T>) ((ParameterizedType) typeT).getRawType();
        }
    }

    @Override
    public void encode(final T object, final Writer writer) throws EncodeException, IOException {
        MAPPER.writeValue(writer, object);
    }

    @Override
    public T decode(final Reader reader) throws DecodeException, IOException {
        return MAPPER.readValue(reader, type);
    }

    @Override
    public void destroy() { // empty
    }

}