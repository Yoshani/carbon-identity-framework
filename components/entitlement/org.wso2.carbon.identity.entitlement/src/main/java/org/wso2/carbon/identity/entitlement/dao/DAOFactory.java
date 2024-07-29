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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.SimplePAPStatusDataHandler;

import static org.wso2.carbon.identity.entitlement.PDPConstants.POLICY_STORAGE_CONFIG;

public class DAOFactory {

    private static final Log log = LogFactory.getLog(DAOFactory.class);
    private static final String POLICY_STORAGE_TYPE = IdentityUtil.getProperty(POLICY_STORAGE_CONFIG);
    private static final String HYBRID = "hybrid";
    private static final String REGISTRY = "registry";

    private DAOFactory() {

    }

    public static PolicyDAO getPolicyDAO() {

        PolicyDAO defaultPolicyDAO = new JDBCPolicyDAOImpl();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    log.info("HybridPolicyDAOImpl initialized.");
                    return new HybridPolicyDAOImpl();
                case REGISTRY:
                    log.info("RegistryPolicyDAOImpl initialized.");
                    return new RegistryPolicyDAOImpl();
                default:
                    log.warn("JDBCPolicyDAOImpl implementation initialized.");
                    return defaultPolicyDAO;
            }
        }
        log.warn("JDBCPolicyDAOImpl implementation initialized.");
        return defaultPolicyDAO;
    }

    public static ConfigDAO getConfigDAO() {

        ConfigDAO defaultConfigDAO = new JDBCConfigDAOImpl();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    log.info("HybridConfigDAOImpl initialized.");
                    return new HybridConfigDAOImpl();
                case REGISTRY:
                    log.info("RegistryConfigDAOImpl initialized.");
                    return new RegistryConfigDAOImpl();
                default:
                    log.warn("JDBCConfigDAOImpl implementation initialized.");
                    return defaultConfigDAO;
            }
        }
        log.warn("JDBCConfigDAOImpl implementation initialized.");
        return defaultConfigDAO;
    }

    public static SubscriberDAO getSubscriberDAO() {

        SubscriberDAO defaultSubscriberDAO = new JDBCSubscriberDAOImpl();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    log.info("HybridSubscriberDAOImpl initialized.");
                    return new HybridSubscriberDAOImpl();
                case REGISTRY:
                    log.info("RegistrySubscriberDAOImpl initialized.");
                    return new RegistrySubscriberDAOImpl();
                default:
                    log.warn("JDBCSubscriberDAOImpl implementation initialized.");
                    return defaultSubscriberDAO;
            }
        }
        log.warn("JDBCSubscriberDAOImpl implementation initialized.");
        return defaultSubscriberDAO;
    }

    public static PAPStatusDataHandler getPAPStatusDataHandler() {

        PAPStatusDataHandler defaultPAPStatusDataHandler = new JDBCSimplePAPStatusDataHandler();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    log.info("HybridPAPStatusDataHandler initialized.");
                    return new HybridPAPStatusDataHandler();
                case REGISTRY:
                    log.info("RegistryPAPStatusDataHandler initialized.");
                    return new SimplePAPStatusDataHandler();
                default:
                    log.warn("JDBCSimplePAPStatusDataHandler implementation initialized.");
                    return defaultPAPStatusDataHandler;
            }
        }
        log.warn("JDBCSimplePAPStatusDataHandler implementation initialized.");
        return defaultPAPStatusDataHandler;
    }
}

