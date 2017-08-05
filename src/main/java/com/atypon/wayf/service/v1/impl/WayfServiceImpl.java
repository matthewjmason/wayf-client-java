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

import com.atypon.wayf.data.WayfEnvironment;
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

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WayfServiceImpl implements WayfSynchronousService {
    private static final Logger LOG = LoggerFactory.getLogger(WayfServiceImpl.class);

    private static final String AUTHORIZATION_HEADER_API_TOKEN_PREFIX = "Token ";

    private String baseUrl;
    private Map<String, String> cachedFullUrls;
    private SerializationHandler serializationHandler;
    private String publisherApiToken;
    private String authorizationHeaderValue;
    private HttpRequestExecutor<HttpRequest> httpRequestExecutor;

    public WayfServiceImpl() {
        this.cachedFullUrls = new HashMap<>();
    }

    public WayfServiceImpl baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public WayfServiceImpl cachedFullUrls(Map<String, String> cachedFullUrls) {
        this.cachedFullUrls = cachedFullUrls;
        return this;
    }

    public WayfServiceImpl serializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        return this;
    }

    public WayfServiceImpl publisherApiToken(String publisherApiToken) {
        this.publisherApiToken = publisherApiToken;
        this.authorizationHeaderValue = buildPublisherTokenAuthorizationValue(publisherApiToken);
        return this;
    }

    public WayfServiceImpl httpRequestExecutor(HttpRequestExecutor<HttpRequest> httpRequestExecutor) {
        this.httpRequestExecutor = httpRequestExecutor;
        return this;
    }

    @Override
    public void registerLocalId(String localId) throws WayfException {
        if (localId == null || localId.isEmpty()) {
            throw new IllegalArgumentException("In order to register a local ID, a non-null and non-empty value is required");
        }

        String encodedLocalId = urlEncode(localId);

        httpRequestExecutor.execute(
                Unirest.post(buildUrl(WayfServiceImpl.REGISTER_LOCAL_ID_URL))
                        .header(PUBLISHER_API_TOKEN_HEADER, authorizationHeaderValue)
                        .routeParam(LOCAL_ID_URL_PARAM, encodedLocalId),
                Void.class
        );
    }

    @Override
    public List<IdentityProviderUsage> getDeviceHistory(String localId) throws WayfException {
        if (localId == null || localId.isEmpty()) {
            throw new IllegalArgumentException("A non-null and non-empty localId is required to read a device's history");
        }

        String encodedLocalId = urlEncode(localId);

        return httpRequestExecutor.execute(
                Unirest.get(buildUrl(WayfServiceImpl.DEVICE_HISTORY_URL))
                        .header(PUBLISHER_API_TOKEN_HEADER, authorizationHeaderValue)
                        .routeParam(LOCAL_ID_URL_PARAM, encodedLocalId),
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

        String encodedLocalId = urlEncode(localId);

        return httpRequestExecutor.execute(
                Unirest.post(buildUrl(WayfService.ADD_IDENTITY_PROVIDER_USAGE_URL))
                        .header(PUBLISHER_API_TOKEN_HEADER, authorizationHeaderValue)
                        .routeParam(LOCAL_ID_URL_PARAM, encodedLocalId)
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

        String encodedLocalId = urlEncode(localId);

        httpRequestExecutor.execute(
                Unirest.delete(buildUrl(WayfService.REMOVE_IDENTITY_PROVIDER_OPTION))
                        .header(PUBLISHER_API_TOKEN_HEADER, authorizationHeaderValue)
                        .routeParam(LOCAL_ID_URL_PARAM, encodedLocalId)
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

    private String urlEncode(String stringToEncode) throws WayfException {
        try {
            return URLEncoder.encode(stringToEncode, "UTF-8");
        } catch (Exception e) {
            throw new WayfException("Could not encode string [" + stringToEncode + "]", e);
        }
    }
}
