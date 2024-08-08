/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;

import java.util.ArrayList;

/**
 * Cache implementation for PAP policy list.
 * Cache entry: <tenant id, policy DTO list>
 */
public class PapPolicyListCache extends BaseCache<Integer, ArrayList<PolicyDTO>> {

    private static final String CACHE_NAME = "PapPolicyListCache";
    private static final PapPolicyListCache instance = new PapPolicyListCache();

    private PapPolicyListCache() {

        super(CACHE_NAME);
    }

    public static PapPolicyListCache getInstance() {

        return instance;
    }
}
