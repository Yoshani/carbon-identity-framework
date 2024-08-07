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
import org.wso2.carbon.identity.entitlement.cache.PapPolicyCache;
import org.wso2.carbon.identity.entitlement.cache.PapPolicyListCache;
import org.wso2.carbon.identity.entitlement.cache.PdpPolicyCache;
import org.wso2.carbon.identity.entitlement.cache.PdpPolicyListCache;
import org.wso2.carbon.identity.entitlement.dao.puredao.PolicyPureDAO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;

import java.util.ArrayList;
import java.util.List;

public class CacheBackedPolicyPureDAO extends PolicyPureDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedPolicyPureDAO.class);
    private final PapPolicyCache papPolicyCache = PapPolicyCache.getInstance();
    private final PapPolicyListCache papPolicyListCache = PapPolicyListCache.getInstance();
    private final PdpPolicyCache pdpPolicyCache = PdpPolicyCache.getInstance();
    private final PdpPolicyListCache pdpPolicyListCache = PdpPolicyListCache.getInstance();

    @Override
    public void insertPolicy(PolicyDTO policy, int tenantId) throws EntitlementException {

        super.insertPolicy(policy, tenantId);
        papPolicyCache.addToCache(policy.getPolicyId(), policy, tenantId);
        papPolicyListCache.clearCacheEntry(tenantId, tenantId);
    }

    @Override
    public PolicyDTO getPAPPolicy(String policyId, int tenantId) throws EntitlementException {

        PolicyDTO policy = papPolicyCache.getValueFromCache(policyId, tenantId);
        if (policy != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit in PapPolicyCache for policy: " + policyId + " for tenant: " + tenantId);
            }
            return policy;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache miss in PapPolicyCache for policy: " + policyId + " for tenant: " + tenantId);
        }
        policy = super.getPAPPolicy(policyId, tenantId);
        papPolicyCache.addToCache(policyId, policy, tenantId);
        papPolicyListCache.clearCacheEntry(tenantId, tenantId);
        return policy;
    }

    @Override
    public List<PolicyDTO> getAllPAPPolicies(int tenantId) throws EntitlementException {

        List<PolicyDTO> policies = papPolicyListCache.getValueFromCache(tenantId, tenantId);
        if (policies != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit in PapPolicyListCache for policies for tenant: " + tenantId);
            }
            return policies;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache miss in PapPolicyListCache for policies for tenant: " + tenantId);
        }
        policies = super.getAllPAPPolicies(tenantId);
        papPolicyListCache.addToCache(tenantId, (ArrayList<PolicyDTO>) policies, tenantId);
        return policies;
    }

    @Override
    public void deletePAPPolicy(String policyId, int tenantId) throws EntitlementException {

        super.deletePAPPolicy(policyId, tenantId);
        papPolicyCache.clearCacheEntry(policyId, tenantId);
        papPolicyListCache.clearCacheEntry(tenantId, tenantId);
    }

    @Override
    public PolicyDTO getPDPPolicy(String policyId, int tenantId) {

        PolicyDTO policy = pdpPolicyCache.getValueFromCache(policyId, tenantId);
        if (policy != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit in PdpPolicyCache for policy: " + policyId + " for tenant: " + tenantId);
            }
            return policy;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache miss in PdpPolicyCache for policy: " + policyId + " for tenant: " + tenantId);
        }
        policy = super.getPDPPolicy(policyId, tenantId);
        pdpPolicyCache.addToCache(policyId, policy, tenantId);
        pdpPolicyListCache.clearCacheEntry(tenantId, tenantId);
        return policy;
    }

    @Override
    public PolicyDTO[] getAllPDPPolicies(int tenantId) throws EntitlementException {

        PolicyDTO[] policies = pdpPolicyListCache.getValueFromCache(tenantId, tenantId);
        if (policies != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit in PdpPolicyListCache for policies for tenant: " + tenantId);
            }
            return policies;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache miss in PdpPolicyListCache for policies for tenant: " + tenantId);
        }
        policies = super.getAllPDPPolicies(tenantId);
        pdpPolicyListCache.addToCache(tenantId, policies, tenantId);
        return policies;
    }

    @Override
    public void insertOrUpdatePolicy(PolicyStoreDTO policy, int tenantId) throws EntitlementException {

    }

    @Override
    public void updateActiveStatusAndOrder(PolicyStoreDTO policy, int tenantId) throws EntitlementException {

    }

    @Override
    public int getPublishedVersion(PolicyStoreDTO policy, int tenantId) throws EntitlementException {

        return -1;
    }

    @Override
    public boolean unpublishPolicy(String policyId, int tenantId) {

        return false;
    }
}
