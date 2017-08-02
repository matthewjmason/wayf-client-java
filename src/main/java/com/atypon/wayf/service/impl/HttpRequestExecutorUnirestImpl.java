/*
 * Copyright 2017 Atypon Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atypon.wayf.service.impl;

import com.atypon.wayf.data.ErrorResponse;
import com.atypon.wayf.data.WayfException;
import com.atypon.wayf.service.HttpRequestExecutor;
import com.atypon.wayf.service.SerializationHandler;
import com.atypon.wayf.service.v1.impl.WayfServiceImpl;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestExecutorUnirestImpl implements HttpRequestExecutor<HttpRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(WayfServiceImpl.class);

    private SerializationHandler serializationHandler;

    public HttpRequestExecutorUnirestImpl(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
    }

    public <T> T execute(HttpRequest request, Class<T> responseClass) throws WayfException {
        HttpResponse<String> response = null;

        LOG.debug("Executing request with method [{}], URL [{}], headers [{}], and body [{}]", request.getHttpMethod(), request.getUrl(), request.getHeaders(), request.getBody());

        try {
            response = request.asString();
        } catch (UnirestException e) {
            LOG.error("Could not execute HTTP request", e);

            throw new WayfException("Could not execute HTTP request", e);
        }

        LOG.debug("WAYF Cloud responded with code [{}], body [{}]", response.getStatus(), response.getBody());

        // If the response was in the error range, the return type will be ErrorResponse. Create an exception with the
        // HTTP response code and error message.
        if (response.getStatus() > 299) {
            ErrorResponse errorResponse = serializationHandler.deserialize(response.getBody(), ErrorResponse.class);

            String message = errorResponse != null? errorResponse.getMessage() : null;
            throw new WayfException(response.getStatus(), message);
        }

        if (responseClass == null || responseClass == Void.class) {
            return null;
        }

        try {
            return serializationHandler.deserialize(response.getBody(), responseClass);
        } catch (Exception e) {
            LOG.error("Could not deserialize response with code [{}], body [{}]", response.getStatus(), response.getBody());

            if (WayfException.class.isAssignableFrom(e.getClass())) {
                throw (WayfException) e;
            } else {
                throw new WayfException("Could not deserialize WAYF response", e);
            }
        }
    }

}
