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

package com.atypon.wayf.service.v1.impl;

import com.atypon.wayf.data.WayfException;
import com.atypon.wayf.data.identity.IdentityProvider;
import com.atypon.wayf.data.identity.IdentityProviderUsage;
import com.atypon.wayf.service.HttpRequestExecutor;
import com.atypon.wayf.service.SerializationHandler;
import com.atypon.wayf.service.v1.WayfService;
import com.atypon.wayf.service.v1.WayfSynchronousService;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WafServiceImpl implements WayfSynchronousService {
    private static final Logger LOG = LoggerFactory.getLogger(WafServiceImpl.class);

    private static final String AUTHORIZATION_HEADER_API_TOKEN_PREFIX = "API ";

    private String baseUrl;
    private Map<String, String> cachedFullUrls;
    private SerializationHandler serializationHandler;
    private String publisherApiToken;
    private String authorizationHeaderValue;
    private HttpRequestExecutor<HttpRequest> httpRequestExecutor;

    public WafServiceImpl() {
        this.cachedFullUrls = new HashMap<>();
    }

    public WafServiceImpl baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public WafServiceImpl cachedFullUrls(Map<String, String> cachedFullUrls) {
        this.cachedFullUrls = cachedFullUrls;
        return this;
    }

    public WafServiceImpl serializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        return this;
    }

    public WafServiceImpl publisherApiToken(String publisherApiToken) {
        this.publisherApiToken = publisherApiToken;
        this.authorizationHeaderValue = buildPublisherTokenAuthorizationValue(publisherApiToken);
        return this;
    }

    public WafServiceImpl httpRequestExecutor(HttpRequestExecutor<HttpRequest> httpRequestExecutor) {
        this.httpRequestExecutor = httpRequestExecutor;
        return this;
    }

    @Override
    public void registerLocalId(String localId) throws WayfException {
        if (localId == null || localId.isEmpty()) {
            throw new IllegalArgumentException("In order to register a local ID, a non-null and non-empty value is required");
        }

        httpRequestExecutor.execute(
                Unirest.post(buildUrl(WafServiceImpl.REGISTER_LOCAL_ID_URL))
                        .header(PUBLISHER_API_TOKEN_HEADER, authorizationHeaderValue)
                        .routeParam(LOCAL_ID_URL_PARAM, localId),
                Void.class
        );
    }

    @Override
    public List<IdentityProviderUsage> getDeviceHistory(String localId) throws WayfException {
        if (localId == null || localId.isEmpty()) {
            throw new IllegalArgumentException("A non-null and non-empty localId is required to read a device's history");
        }

        return httpRequestExecutor.execute(
                Unirest.get(buildUrl(WafServiceImpl.DEVICE_HISTORY_URL))
                        .header(PUBLISHER_API_TOKEN_HEADER, authorizationHeaderValue)
                        .routeParam(LOCAL_ID_URL_PARAM, localId),
                List.class
        );
    }

    @Override
    public IdentityProvider addIdentityProviderUsage(String localId, IdentityProvider identityProvider) throws WayfException {
        if (localId == null || localId.isEmpty()) {
            throw new IllegalArgumentException("A non-null and non-empty localId is required to add an IdentityProvider usage to a device");
        }

        if (identityProvider == null) {
            throw new IllegalArgumentException("A non-null IdentityProvider is required to add an IdentityProvider usage to a device");
        }

        return httpRequestExecutor.execute(
                Unirest.post(buildUrl(WayfService.ADD_IDENTITY_PROVIDER_USAGE_URL))
                        .header(PUBLISHER_API_TOKEN_HEADER, authorizationHeaderValue)
                        .routeParam(LOCAL_ID_URL_PARAM, localId)
                        .body(serializationHandler.serialize(identityProvider))
                        .getHttpRequest(),
                IdentityProvider.class
        );
    }

    @Override
    public void removeIdentityProviderOption(String localId, Long identityProviderId) throws WayfException{
        if (localId == null || localId.isEmpty()) {
            throw new IllegalArgumentException("A non-null and non-empty localId is required to remove an IdentityProvider from a device");
        }

        if (identityProviderId == null) {
            throw new IllegalArgumentException("A non-null IdentityProvider ID is required to remove an IdentityProvider from a device");
        }

        httpRequestExecutor.execute(
                Unirest.delete(buildUrl(WayfService.REMOVE_IDENTITY_PROVIDER_OPTION))
                        .header(PUBLISHER_API_TOKEN_HEADER, authorizationHeaderValue)
                        .routeParam(LOCAL_ID_URL_PARAM, localId)
                        .routeParam(IDP_ID, identityProviderId.toString()),
                Void.class
        );
    }

    private String buildUrl(String requestPath) {
        String fullUrl = cachedFullUrls.get(requestPath);

        if (fullUrl == null) {
            fullUrl = baseUrl + requestPath;
            cachedFullUrls.put(requestPath, fullUrl);
        }

        return fullUrl;
    }

    private String buildPublisherTokenAuthorizationValue(String publisherApiToken) {
        return AUTHORIZATION_HEADER_API_TOKEN_PREFIX + publisherApiToken;
    }
}
