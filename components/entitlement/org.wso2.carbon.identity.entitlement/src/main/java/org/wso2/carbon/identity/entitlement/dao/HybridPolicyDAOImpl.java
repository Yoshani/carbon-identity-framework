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

package org.wso2.carbon.identity.entitlement.dao;

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.policy.finder.AbstractPolicyFinderModule;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class HybridPolicyDAOImpl extends AbstractPolicyFinderModule implements PolicyDAO {

    private final JDBCPolicyDAOImpl jdbcPolicyDAO = new JDBCPolicyDAOImpl();
    private final RegistryPolicyDAOImpl registryPolicyDAO = new RegistryPolicyDAOImpl();

    @Override
    public void init(Properties properties) throws Exception {

        registryPolicyDAO.init(properties);
    }

    @Override
    public void addOrUpdatePolicy(PolicyDTO policy, boolean enableVersioning) throws EntitlementException {

        if (!jdbcPolicyDAO.isExists(policy.getPolicyId()) && !enableVersioning) {
            registryPolicyDAO.addOrUpdatePolicy(policy, false);
        }
        jdbcPolicyDAO.addOrUpdatePolicy(policy, enableVersioning);
    }

    @Override
    public PolicyDTO getPAPPolicy(String policyId) throws EntitlementException {

        PolicyDTO policyDTO = jdbcPolicyDAO.getPAPPolicy(policyId);
        if (policyDTO == null) {
            policyDTO = registryPolicyDAO.getPAPPolicy(policyId);
        }
        return policyDTO;
    }

    @Override
    public List<PolicyDTO> getPAPPolicies(List<String> policyIds) throws EntitlementException {

        List<PolicyDTO> policyDTOs = jdbcPolicyDAO.getPAPPolicies(policyIds);
        List<PolicyDTO> regPolicyDTOs = registryPolicyDAO.getPAPPolicies(policyIds);
        return EntitlementUtil.mergeAndRemoveDuplicates(policyDTOs, regPolicyDTOs);
    }

    @Override
    public PolicyDTO getPolicy(String policyId, String version) throws EntitlementException {

        PolicyDTO policyDTO;
        try {
            policyDTO = jdbcPolicyDAO.getPolicy(policyId, version);
        } catch (EntitlementException e) {
            policyDTO = registryPolicyDAO.getPolicy(policyId, version);
        }
        return policyDTO;
    }

    @Override
    public String[] getVersions(String policyId) {

        List<String> versions = Arrays.asList(jdbcPolicyDAO.getVersions(policyId));
        List<String> regVersions = Arrays.asList(registryPolicyDAO.getVersions(policyId));
        return EntitlementUtil.mergeAndRemoveDuplicates(versions, regVersions).toArray(new String[0]);
    }

    @Override
    public List<String> listPolicyIds() throws EntitlementException {

        List<String> policyIds = jdbcPolicyDAO.listPolicyIds();
        List<String> regPolicyIds = registryPolicyDAO.listPolicyIds();
        return EntitlementUtil.mergeAndRemoveDuplicates(policyIds, regPolicyIds);
    }

    @Override
    public void removePolicy(String policyId) throws EntitlementException {

        if (jdbcPolicyDAO.isExists(policyId)) {
            jdbcPolicyDAO.removePolicy(policyId);
        }
        registryPolicyDAO.removePolicy(policyId);
    }

    @Override
    public void publishPolicy(PolicyStoreDTO policy) throws EntitlementException {

        if (jdbcPolicyDAO.isExists(policy.getPolicyId())) {
            jdbcPolicyDAO.publishPolicy(policy);
        } else {
            registryPolicyDAO.publishPolicy(policy);
        }
    }

    @Override
    public boolean isPublished(String policyId) {

        return jdbcPolicyDAO.isPublished(policyId) || registryPolicyDAO.isPublished(policyId);
    }

    @Override
    public PolicyDTO getPublishedPolicy(String policyId) {

        PolicyDTO policyDTO = jdbcPolicyDAO.getPublishedPolicy(policyId);
        if (policyDTO == null || policyDTO.getPolicy() == null) {
            policyDTO = registryPolicyDAO.getPublishedPolicy(policyId);
        }
        return policyDTO;
    }

    @Override
    public List<String> listPublishedPolicyIds() throws EntitlementException {

        List<String> dbPolicyIds = jdbcPolicyDAO.listPublishedPolicyIds();
        List<String> regPolicyIds = registryPolicyDAO.listPublishedPolicyIds();
        return EntitlementUtil.mergeAndRemoveDuplicates(dbPolicyIds, regPolicyIds);
    }

    @Override
    public void unPublishPolicy(String policyId) {

        if (jdbcPolicyDAO.isExists(policyId)) {
            jdbcPolicyDAO.unPublishPolicy(policyId);
        } else {
            registryPolicyDAO.unPublishPolicy(policyId);
        }
    }

    @Override
    protected String[] getPolicyIdentifiers() {

        String[] dbPolicyIds = jdbcPolicyDAO.getPolicyIdentifiers();
        String[] regPolicyIds = registryPolicyDAO.getPolicyIdentifiers();
        return EntitlementUtil.mergeAndRemoveDuplicates(Arrays.asList(dbPolicyIds), Arrays.asList(regPolicyIds))
                .toArray(new String[0]);
    }

    @Override
    public String getModuleName() {

        return jdbcPolicyDAO.getModuleName();
    }

    @Override
    public String getPolicy(String policyId) {

        String policy = jdbcPolicyDAO.getPolicy(policyId);
        if (policy == null) {
            policy = registryPolicyDAO.getPolicy(policyId);
        }
        return policy;
    }

    @Override
    public int getPolicyOrder(String policyId) {

        int policyOrder = jdbcPolicyDAO.getPolicyOrder(policyId);
        if (policyOrder == -1) {
            policyOrder = registryPolicyDAO.getPolicyOrder(policyId);
        }
        return policyOrder;
    }

    @Override
    public String getReferencedPolicy(String policyId) {

        String policy = jdbcPolicyDAO.getReferencedPolicy(policyId);
        if (policy == null) {
            policy = registryPolicyDAO.getReferencedPolicy(policyId);
        }
        return policy;
    }
}
