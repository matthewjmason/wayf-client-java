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

package com.atypon.wayf.service.v1;

import com.atypon.wayf.data.WayfEnvironment;
import com.atypon.wayf.service.HttpRequestExecutor;
import com.atypon.wayf.service.SerializationHandler;
import com.atypon.wayf.service.impl.HttpRequestExecutorUnirestImpl;
import com.atypon.wayf.service.impl.SerializationHandlerObjectMapperImpl;
import com.atypon.wayf.service.v1.impl.WafServiceImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class WayfClient {
    private static final String SANDBOX_URL_PROPERTY = "sandbox.url";
    private static final String PRODUCTION_URL_PROPERTY = "production.url";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

    private static SerializationHandler serializationHandler ;
    private static Properties properties;
    private static Map<WayfEnvironment, String> environmentToUrlMap;
    private static HttpRequestExecutor httpRequestExecutor;

    private String publisherToken;
    private WayfEnvironment environment;

    public static WayfClient connect() {
        initEnvironmentToUrlMap();
        initSerializationHandler();

        return new WayfClient();
    }

    public WayfClient to(WayfEnvironment environment) {
        this.environment = environment;
        return this;
    }

    public WayfClient as(String publisherApiToken) {
        this.publisherToken = publisherApiToken;
        return this;
    }

    public WayfSynchronousService synchronously() {
        if (publisherToken == null || publisherToken.isEmpty()) {
            throw new IllegalArgumentException("A non-null and non-empty API token is required to use the WAYF service");
        }

        if (environment == null) {
            throw new IllegalArgumentException("An environment must be specified to use the WAYF service");
        }

        String baseUrl = environmentToUrlMap.get(environment);
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new RuntimeException("Could not find WAYF URL for environment [" + environment + "]");
        }

        return new WafServiceImpl()
                .baseUrl(baseUrl)
                .publisherApiToken(publisherToken)
                .serializationHandler(serializationHandler)
                .httpRequestExecutor(httpRequestExecutor);
    }

    private static void initSerializationHandler() {
        if (serializationHandler != null) {
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(DATE_FORMAT);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        serializationHandler = new SerializationHandlerObjectMapperImpl(objectMapper);
        httpRequestExecutor = new HttpRequestExecutorUnirestImpl(serializationHandler);
    }

    private static void initProperties() {
        properties = new Properties();

        InputStream envProperties = Thread.currentThread().getContextClassLoader().getResourceAsStream("wayf-environment.properties");

        try (Reader reader = new InputStreamReader(envProperties)){
            properties.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Could not read WAYF environment properties");
        }
    }

    private static void initEnvironmentToUrlMap() {
        if (environmentToUrlMap != null) {
            return;
        }

        if (properties == null) {
            initProperties();
        }

        String sandboxUrl = properties.getProperty(SANDBOX_URL_PROPERTY);

        if (sandboxUrl == null || sandboxUrl.isEmpty()) {
            throw new RuntimeException("Could not determine the sandbox environment's URL");
        }

        String productionUrl = properties.getProperty(PRODUCTION_URL_PROPERTY);

        if (productionUrl == null || productionUrl.isEmpty()) {
            throw new RuntimeException("Could not determine the production environment's URL");
        }

        environmentToUrlMap = new HashMap<>();
        environmentToUrlMap.put(WayfEnvironment.SANDBOX, sandboxUrl);
        environmentToUrlMap.put(WayfEnvironment.PRODUCTION, productionUrl);
    }
}
