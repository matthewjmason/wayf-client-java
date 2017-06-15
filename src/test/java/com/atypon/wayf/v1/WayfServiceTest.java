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

package com.atypon.wayf.service;

import com.atypon.wayf.data.WayfEnvironment;
import com.atypon.wayf.data.WayfException;
import com.atypon.wayf.data.identity.IdentityProvider;
import com.atypon.wayf.data.identity.IdentityProviderUsage;
import com.atypon.wayf.data.identity.OauthEntity;
import com.atypon.wayf.data.identity.OauthProvider;
import com.atypon.wayf.service.v1.WayfClient;
import com.atypon.wayf.service.v1.WayfSynchronousService;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WayfServiceTest {
    private static final String API_TOKEN = "0a014c9e-77eb-4215-b817-4c4bdbf0f605";
    private static final String LOCAL_ID = "local-id-publisher-a-4a3d8e3a-63d4-426a-b12b-0d75eb7cfad3";

    @Test
    @Ignore
    public void testClient() throws WayfException{

        WayfSynchronousService wayf = WayfClient.connect().to(WayfEnvironment.SANDBOX).as(API_TOKEN).synchronously();

        wayf.registerLocalId("test-local-id-" + UUID.randomUUID().toString());
        List<IdentityProviderUsage> usageHistory = wayf.getDeviceHistory(LOCAL_ID);

        assertNotNull(usageHistory);
        assertTrue(!usageHistory.isEmpty());

        OauthEntity oauthEntity = new OauthEntity();
        oauthEntity.setProvider(OauthProvider.GOOGLE);

        IdentityProvider idp = wayf.addIdentityProviderUsage(LOCAL_ID, oauthEntity);
        assertNotNull(idp.getId());

        wayf.removeIdentityProviderOption(LOCAL_ID, idp.getId());
    }
}
