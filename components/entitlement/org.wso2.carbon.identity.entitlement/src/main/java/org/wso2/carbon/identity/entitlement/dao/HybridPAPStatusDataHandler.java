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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.SimplePAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class HybridPAPStatusDataHandler implements PAPStatusDataHandler {

    private final JDBCSimplePAPStatusDataHandler jdbcSimplePAPStatusDataHandler = new JDBCSimplePAPStatusDataHandler();
    private final SimplePAPStatusDataHandler registrySimplePAPStatusDataHandler = new SimplePAPStatusDataHandler();
    private static final String ENTITLEMENT_POLICY_STATUS = "/repository/identity/entitlement/status/policy/";
    private static final String ENTITLEMENT_PUBLISHER_STATUS = "/repository/identity/entitlement/status/publisher/";
    private static final Log LOG = LogFactory.getLog(HybridPAPStatusDataHandler.class);

    @Override
    public void init(Properties properties) {

        jdbcSimplePAPStatusDataHandler.init(properties);
        registrySimplePAPStatusDataHandler.init(properties);
    }

    @Override
    public void handle(String about, String key, List<StatusHolder> statusHolders) throws EntitlementException {

        String path = EntitlementConstants.Status.ABOUT_POLICY.equals(about) ? ENTITLEMENT_POLICY_STATUS + key :
                ENTITLEMENT_PUBLISHER_STATUS + key;
        // If the action is DELETE_POLICY, delete the policy or the subscriber status
        for (StatusHolder holder : statusHolders) {
            if (EntitlementConstants.StatusTypes.DELETE_POLICY.equals(holder.getType())) {
                jdbcSimplePAPStatusDataHandler.deletePersistedData(about, key);
                registrySimplePAPStatusDataHandler.deletedPersistedData(path);
                return;
            }
        }
        migrateStatusesToDatabase(about, key, path);
        jdbcSimplePAPStatusDataHandler.persistStatus(about, key, statusHolders);
    }

    @Override
    public void handle(String about, StatusHolder statusHolder) throws EntitlementException {

        List<StatusHolder> list = new ArrayList<>();
        list.add(statusHolder);
        handle(about, statusHolder.getKey(), list);
    }

    @Override
    public StatusHolder[] getStatusData(String about, String key, String type, String searchString)
            throws EntitlementException {

        List<StatusHolder> dbStatusHoldersList =
                Arrays.asList(jdbcSimplePAPStatusDataHandler.getStatusData(about, key, type, searchString));
        List<StatusHolder> regStatusHoldersList =
                Arrays.asList(registrySimplePAPStatusDataHandler.getStatusData(about, key, type, searchString));
        List<StatusHolder> mergedStatusHoldersList =
                EntitlementUtil.mergeAndRemoveDuplicates(dbStatusHoldersList, regStatusHoldersList);
        return new StatusHolder[mergedStatusHoldersList.size()];
    }

    private synchronized void migrateStatusesToDatabase(String about, String key, String path) {

        try {
            StatusHolder[] regStatusHolders =
                    registrySimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
            if (regStatusHolders != null && regStatusHolders.length > 0) {
                List<StatusHolder> regStatusHolderList = Arrays.asList(regStatusHolders);
                jdbcSimplePAPStatusDataHandler.handle(about, key, regStatusHolderList);
            }
            registrySimplePAPStatusDataHandler.deletedPersistedData(path);
        } catch (EntitlementException e) {
            LOG.error("Error while migrating statuses to database", e);
        }
    }
}
