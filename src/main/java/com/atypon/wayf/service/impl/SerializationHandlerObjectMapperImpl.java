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

import com.atypon.wayf.data.WayfException;
import com.atypon.wayf.service.SerializationHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationHandlerObjectMapperImpl implements SerializationHandler {
    private ObjectMapper objectMapper;

    public SerializationHandlerObjectMapperImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T deserialize(String json, Class<T> returnType) throws WayfException {
        try {
            return objectMapper.readValue(json, returnType);
        } catch (Exception e) {
            throw new WayfException("Could not deserialize type", e);
        }
    }

    @Override
    public String serialize(Object toSerialize) throws WayfException {
        try {
            return objectMapper.writeValueAsString(toSerialize);
        } catch (Exception e) {
            throw new WayfException("Could not serialize type", e);
        }
    }
}
