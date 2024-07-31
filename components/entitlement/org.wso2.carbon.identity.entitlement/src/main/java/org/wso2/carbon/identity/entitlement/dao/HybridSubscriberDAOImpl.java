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
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;

import java.util.List;

/**
 * Only new subscribers will be added to DB. Updating existing subscribers will not migrate.
 */
public class HybridSubscriberDAOImpl implements SubscriberDAO {

    private final JDBCSubscriberDAOImpl jdbcSubscriberDAO = new JDBCSubscriberDAOImpl();
    private final RegistrySubscriberDAOImpl registrySubscriberDAO = new RegistrySubscriberDAOImpl();

    @Override
    public void addSubscriber(PublisherDataHolder holder) throws EntitlementException {

        jdbcSubscriberDAO.addSubscriber(holder);
    }

    @Override
    public PublisherDataHolder getSubscriber(String subscriberId, boolean returnSecrets) throws EntitlementException {

        PublisherDataHolder holder = jdbcSubscriberDAO.getSubscriber(subscriberId, returnSecrets);
        if (holder == null) {
            holder = registrySubscriberDAO.getSubscriber(subscriberId, returnSecrets);
        }
        return holder;
    }

    @Override
    public List<String> listSubscriberIds(String filter) throws EntitlementException {

        List<String> subscriberIds = jdbcSubscriberDAO.listSubscriberIds(filter);
        List<String> registrySubscriberIds = registrySubscriberDAO.listSubscriberIds(filter);
        return EntitlementUtil.mergeAndRemoveDuplicates(subscriberIds, registrySubscriberIds);
    }

    @Override
    public void updateSubscriber(PublisherDataHolder holder) throws EntitlementException {

        String subscriberId = EntitlementUtil.resolveSubscriberId(holder);
        if (jdbcSubscriberDAO.isSubscriberExists(subscriberId)) {
            jdbcSubscriberDAO.updateSubscriber(holder);
        } else {
            registrySubscriberDAO.updateSubscriber(holder);
        }
    }

    @Override
    public void removeSubscriber(String subscriberId) throws EntitlementException {

        if (jdbcSubscriberDAO.isSubscriberExists(subscriberId)) {
            jdbcSubscriberDAO.removeSubscriber(subscriberId);
        } else {
            registrySubscriberDAO.removeSubscriber(subscriberId);
        }
    }
}
