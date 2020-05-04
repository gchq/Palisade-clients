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
package uk.gov.gchq.palisade.clients.simpleclient.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.exception.ForbiddenException;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.request.Policy;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * This class is used in the request to set a {@link Policy} for a {@link Resource}.
 * That resource may be signifying a file, stream, directory or the system
 * (policy is applied to all requests to the Palisade system).
 */
@JsonIgnoreProperties(value = {"originalRequestId"})
public class SetResourcePolicyRequest extends Request {
    private Resource resource;
    private Policy policy;

    // no-args constructor
    public SetResourcePolicyRequest() {
    }

    /**
     * @param resource the {@link Resource} to set the {@link Policy} for
     * @return the {@link SetResourcePolicyRequest}
     */
    public SetResourcePolicyRequest resource(final Resource resource) {
        requireNonNull(resource, "The resource cannot be set to null.");
        this.resource = resource;
        return this;
    }

    /**
     * @param policy the {@link Policy} to set for the {@link Resource}
     * @return the {@link SetResourcePolicyRequest}
     */
    public SetResourcePolicyRequest policy(final Policy policy) {
        requireNonNull(policy, "The policy cannot be set to null.");
        this.policy = policy;
        return this;
    }

    public Resource getResource() {
        requireNonNull(resource, "The resource has not been set.");
        return resource;
    }

    public void setResource(final Resource resource) {
        resource(resource);
    }

    public Policy getPolicy() {
        requireNonNull(policy, "The policy has not been set.");
        return policy;
    }

    public void setPolicy(final Policy policy) {
        policy(policy);
    }


    @Override
    public void setOriginalRequestId(final RequestId originalRequestId) {
        throw new ForbiddenException("Should not call SetResourcePolicyRequest.setOriginalRequestId()");
    }

    @Override
    public RequestId getOriginalRequestId() {
        throw new ForbiddenException("Should not call SetResourcePolicyRequest.getOriginalRequestId()");
    }


    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SetResourcePolicyRequest)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final SetResourcePolicyRequest that = (SetResourcePolicyRequest) o;
        return Objects.equals(resource, that.resource) &&
                Objects.equals(policy, that.policy);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), resource, policy);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", SetResourcePolicyRequest.class.getSimpleName() + "[", "]")
                .add("resource=" + resource)
                .add("policy=" + policy)
                .toString();
    }
}
