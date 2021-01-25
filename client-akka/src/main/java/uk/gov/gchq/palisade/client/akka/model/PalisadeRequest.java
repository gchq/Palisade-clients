/*
 * Copyright 2021 Crown Copyright
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
package uk.gov.gchq.palisade.client.akka.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.Generated;

import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents the original data that has been sent from the client to Palisade Service for a request to access data.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class PalisadeRequest {

    private final String userId;  //Unique identifier for the user.
    private final String resourceId;  //Resource that that is being asked to access.

    // Ignore class type on context object
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = Context.class)
    private final Map<String, String> context;

    @JsonCreator
    private PalisadeRequest(
            final @JsonProperty("userId") String userId,
            final @JsonProperty("resourceId") String resourceId,
            final @JsonProperty("context") Map<String, String> context) {

        this.userId = Optional.ofNullable(userId).orElseThrow(() -> new IllegalArgumentException("User ID cannot be null"));
        this.resourceId = Optional.ofNullable(resourceId).orElseThrow(() -> new IllegalArgumentException("Resource ID  cannot be null"));
        this.context = Optional.ofNullable(context).orElseThrow(() -> new IllegalArgumentException("Context cannot be null"));
    }

    @Generated
    public String getUserId() {
        return userId;
    }

    @Generated
    public String getResourceId() {
        return resourceId;
    }

    @Generated
    public Map<String, String> getContext() {
        return context;
    }

    /**
     * Builder class for the creation of the PalisadeRequest.  This is a variant of the Fluent Builder
     * which will use String or optionally JsonNodes for the components in the build.
     */
    public static class Builder {

        /**
         * Starter method for the Builder class.  This method is called to start the process of creating the
         * PalisadeRequest class.
         *
         * @return interface  {@link IUserId} for the next step in the build.
         */
        public static IUserId create() {
            return userId -> resourceId -> context ->
                    new PalisadeRequest(userId, resourceId, context);
        }

        /**
         * Adds the user ID information to the message.
         */
        public interface IUserId {
            /**
             * Adds the user's ID.
             *
             * @param userId user ID for the request.
             * @return interface  {@link IResourceId} for the next step in the build.
             */
            IResourceId withUserId(String userId);
        }

        /**
         * Adds the resource ID information to the message.
         */
        public interface IResourceId {
            /**
             * Adds the resource ID.
             *
             * @param resourceId resource ID for the request.
             * @return interface {@link IContext} for the next step in the build.
             */
            IContext withResourceId(String resourceId);
        }

        /**
         * Adds the user context information to the message.
         */
        public interface IContext {
            /**
             * Adds the user context information.
             *
             * @param context information about this request.
             * @return class {@link PalisadeRequest} this builder is set-up to create.
             */
            PalisadeRequest withContext(Map<String, String> context);
        }
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", PalisadeRequest.class.getSimpleName() + "[", "]")
                .add("userId='" + userId + "'")
                .add("resourceId='" + resourceId + "'")
                .add("context=" + context)
                .toString();
    }
}
