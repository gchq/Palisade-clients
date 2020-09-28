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

import uk.gov.gchq.palisade.client.java.util.ClientUtil;

import javax.websocket.*;

import java.io.*;
import java.lang.reflect.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JSONCoder<T> implements Encoder.TextStream<T>, Decoder.TextStream<T> {

    private Class<T> _type;

    private ThreadLocal<ObjectMapper> _mapper = new ThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
            return ClientUtil.getObjectMapper();
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    public void init(EndpointConfig endpointConfig) {
        ParameterizedType thisClass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type T = thisClass.getActualTypeArguments()[0];
        if (T instanceof Class) {
            _type = (Class<T>) T;
        } else if (T instanceof ParameterizedType) {
            _type = (Class<T>) ((ParameterizedType) T).getRawType();
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