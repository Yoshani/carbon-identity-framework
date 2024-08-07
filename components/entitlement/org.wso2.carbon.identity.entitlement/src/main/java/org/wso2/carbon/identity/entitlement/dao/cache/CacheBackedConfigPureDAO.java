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

package org.wso2.carbon.identity.entitlement.dao.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.cache.ConfigCache;
import org.wso2.carbon.identity.entitlement.dao.puredao.ConfigPureDAO;

public class CacheBackedConfigPureDAO extends ConfigPureDAO {

    private static final CacheBackedConfigPureDAO instance = new CacheBackedConfigPureDAO();

    public static CacheBackedConfigPureDAO getInstance() {

        return instance;
    }

    private static final Log LOG = LogFactory.getLog(CacheBackedConfigPureDAO.class);
    private final ConfigCache configCache = ConfigCache.getInstance();

    @Override
    public String getPolicyCombiningAlgorithm(int tenantId) throws EntitlementException {

        String algorithm = configCache.getValueFromCache(PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM, tenantId);
        if (algorithm != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit in ConfigCache for policy combining algorithm for tenant: " + tenantId);
            }
            return algorithm;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache miss in ConfigCache for policy combining algorithm for tenant: " + tenantId);
        }
        algorithm = super.getPolicyCombiningAlgorithm(tenantId);
        configCache.addToCache(PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM, algorithm, tenantId);

        return algorithm;
    }

    @Override
    public void insertPolicyCombiningAlgorithm(String policyCombiningAlgorithm, int tenantId)
            throws EntitlementException {

        super.insertPolicyCombiningAlgorithm(policyCombiningAlgorithm, tenantId);
        configCache.addToCache(PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM, policyCombiningAlgorithm, tenantId);
    }

    @Override
    public void updatePolicyCombiningAlgorithm(String policyCombiningAlgorithm, int tenantId)
            throws EntitlementException {

        super.updatePolicyCombiningAlgorithm(policyCombiningAlgorithm, tenantId);
        configCache.addToCache(PDPConstants.GLOBAL_POLICY_COMBINING_ALGORITHM, policyCombiningAlgorithm, tenantId);
    }
}
