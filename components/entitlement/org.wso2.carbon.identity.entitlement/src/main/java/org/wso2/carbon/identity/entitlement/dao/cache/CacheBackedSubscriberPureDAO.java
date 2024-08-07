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
import org.wso2.carbon.identity.entitlement.cache.SubscriberCache;
import org.wso2.carbon.identity.entitlement.cache.SubscriberListCache;
import org.wso2.carbon.identity.entitlement.dao.puredao.SubscriberPureDAO;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;

import java.util.ArrayList;
import java.util.List;

public class CacheBackedSubscriberPureDAO extends SubscriberPureDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedSubscriberPureDAO.class);
    private final SubscriberCache subscriberCache = SubscriberCache.getInstance();
    private final SubscriberListCache subscriberListCache = SubscriberListCache.getInstance();

    @Override
    public PublisherDataHolder getSubscriber(String subscriberId, int tenantId)
            throws EntitlementException {

        PublisherDataHolder subscriber = subscriberCache.getValueFromCache(subscriberId, tenantId);
        if (subscriber != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit in SubscriberCache for subscriber: " + subscriberId + " for tenant: " + tenantId);
            }
            return subscriber;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache miss in SubscriberCache for subscriber: " + subscriberId + " for tenant: " + tenantId);
        }
        subscriber = super.getSubscriber(subscriberId, tenantId);
        subscriberCache.addToCache(subscriberId, subscriber, tenantId);
        return subscriber;
    }

    @Override
    public List<String> getSubscriberIds(int tenantId) throws EntitlementException {

        List<String> subscriberIds = subscriberListCache.getValueFromCache(tenantId, tenantId);
        if (subscriberIds != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit in SubscriberListCache for subscriber ids for tenant: " + tenantId);
            }
            return subscriberIds;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache miss in SubscriberListCache for subscriber ids for tenant: " + tenantId);
        }
        subscriberIds = super.getSubscriberIds(tenantId);
        subscriberListCache.addToCache(tenantId, (ArrayList<String>) subscriberIds, tenantId);
        return subscriberIds;
    }

    @Override
    public void insertSubscriber(String subscriberId, PublisherDataHolder holder, int tenantId)
            throws EntitlementException {

        super.insertSubscriber(subscriberId, holder, tenantId);

        subscriberCache.addToCache(subscriberId, holder, tenantId);
        subscriberListCache.clearCacheEntry(tenantId, tenantId);
    }

    @Override
    public void updateSubscriber(String subscriberId, String updatedModuleName,
                                 PublisherPropertyDTO[] updatedPropertyDTOS, int tenantId)
            throws EntitlementException {

        super.updateSubscriber(subscriberId, updatedModuleName, updatedPropertyDTOS, tenantId);
        PublisherDataHolder holder = getSubscriber(subscriberId, tenantId);
        subscriberCache.addToCache(subscriberId, holder, tenantId);
        subscriberListCache.clearCacheEntry(tenantId, tenantId);
    }

    @Override
    public void deleteSubscriber(String subscriberId, int tenantId) throws EntitlementException {

        super.deleteSubscriber(subscriberId, tenantId);
        subscriberCache.clearCacheEntry(subscriberId, tenantId);
        subscriberListCache.clearCacheEntry(tenantId, tenantId);
    }
}
