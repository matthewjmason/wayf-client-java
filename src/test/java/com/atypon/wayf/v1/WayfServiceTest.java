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
    private static final String API_TOKEN = "ef3177ed-17d3-4a54-a7d3-99905c1ec109";
    private static final String LOCAL_ID = "= K01GfwwD4BWB0lgWScJwIfMZOSIp9Wgsw7i0Gcj7J/8cUKRDktD9/A==";

    @Test
    @Ignore
    public void testClient() throws WayfException{

        WayfSynchronousService wayf = WayfClient.connect().to(WayfEnvironment.SANDBOX).as(API_TOKEN).synchronously();

        wayf.registerLocalId("= K01GfwwD4BWB0lgWScJwIfMZOSIp9Wgsw7i0Gcj7J/8cUKRDktD9/A==");

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
