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
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.SimplePAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class HybridPAPStatusDataHandler implements PAPStatusDataHandler {

    private final JDBCSimplePAPStatusDataHandler jdbcSimplePAPStatusDataHandler = new JDBCSimplePAPStatusDataHandler();
    private final SimplePAPStatusDataHandler registrySimplePAPStatusDataHandler = new SimplePAPStatusDataHandler();

    @Override
    public void init(Properties properties) {

        jdbcSimplePAPStatusDataHandler.init(properties);
        registrySimplePAPStatusDataHandler.init(properties);
    }

    @Override
    public void handle(String about, String key, List<StatusHolder> statusHolders) throws EntitlementException {

        List<StatusHolder> regStatusHoldersList =
                Arrays.asList(registrySimplePAPStatusDataHandler.getStatusData(about, key, null, "*"));
        if (regStatusHoldersList.isEmpty()) {
            jdbcSimplePAPStatusDataHandler.handle(about, key, statusHolders);
        } else {
            registrySimplePAPStatusDataHandler.handle(about, key, statusHolders);
        }
    }

    @Override
    public StatusHolder[] getStatusData(String about, String key, String type, String searchString)
            throws EntitlementException {

        StatusHolder[] statusHolders = jdbcSimplePAPStatusDataHandler.getStatusData(about, key, type, searchString);
        if (statusHolders.length == 0) {
            statusHolders = registrySimplePAPStatusDataHandler.getStatusData(about, key, type, searchString);
        }
        return statusHolders;
    }
}
