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

import javax.websocket.*;

import java.io.*;
import java.lang.reflect.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

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
 * @author dbell
 * @since 0.5.0
 * @param <T> The type of object to be serialised to and deserialised from
 */
public abstract class JSONCoder<T> implements Encoder.TextStream<T>, Decoder.TextStream<T> {

    /**
     * The type
     */
    private Class<T> _type;

    /**
     * Store the object mapper in each thread
     */
    private ThreadLocal<ObjectMapper> _mapper = new ThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
            return new ObjectMapper().registerModule(new GuavaModule()).registerModule(new Jdk8Module());
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    public void init(EndpointConfig endpointConfig) {
        ParameterizedType thisClass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type typeT = thisClass.getActualTypeArguments()[0];
        if (typeT instanceof Class) {
            _type = (Class<T>) typeT;
        } else if (typeT instanceof ParameterizedType) {
            _type = (Class<T>) ((ParameterizedType) typeT).getRawType();
        }
    }

    @Override
    public void encode(T object, Writer writer) throws EncodeException, IOException {
        _mapper.get().writeValue(writer, object);
    }

    @Override
    public T decode(Reader reader) throws DecodeException, IOException {
        return _mapper.get().readValue(reader, _type);
    }

    @Override
    public void destroy() { // empty
    }

}