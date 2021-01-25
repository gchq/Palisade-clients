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

package uk.gov.gchq.palisade.clients.simpleclient.request;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

public class ReadRequest extends Request {
    private String token;
    private LeafResource resource;

    public ReadRequest token(final String token) {
        requireNonNull(token, "The token cannot be set to null.");
        this.token = token;
        return this;
    }

    public ReadRequest resource(final LeafResource resource) {
        requireNonNull(resource, "The resource cannot be set to null.");
        this.resource = resource;
        return this;
    }

    public String getToken() {
        requireNonNull(token, "The token has not been set.");
        return token;
    }

    public void setToken(final String token) {
        token(token);
    }

    public LeafResource getResource() {
        requireNonNull(resource, "The resource has not been set.");
        return resource;
    }

    public void setResource(final LeafResource resource) {
        resource(resource);
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadRequest)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ReadRequest that = (ReadRequest) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(resource, that.resource);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), token, resource);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", ReadRequest.class.getSimpleName() + "[", "]")
                .add("token='" + token + "'")
                .add("resource=" + resource)
                .toString();
    }
}
