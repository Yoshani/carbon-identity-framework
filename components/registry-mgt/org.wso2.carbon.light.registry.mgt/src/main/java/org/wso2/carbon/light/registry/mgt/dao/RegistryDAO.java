/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.light.registry.mgt.dao;

import org.wso2.carbon.light.registry.mgt.LightRegistryException;
import org.wso2.carbon.light.registry.mgt.model.CollectionImpl;
import org.wso2.carbon.light.registry.mgt.model.ResourceID;
import org.wso2.carbon.light.registry.mgt.model.ResourceImpl;

import java.util.List;

/**
 * The data access object for resources.
 */
public interface RegistryDAO {

    /**
     * Method to check the resource existence for a given path.
     *
     * @param resourceId the resource id
     * @param tenantId   the tenant id
     * @return true, if the resource exists, false otherwise
     * @throws LightRegistryException throws if checking existence failed.
     */
    boolean resourceExists(ResourceID resourceId, int tenantId) throws LightRegistryException;

    /**
     * Method to get the path id for a given path.
     *
     * @param path the path to get the path id.
     * @return the path id.
     * @throws LightRegistryException throws if the operation failed.
     */
    int getPathID(String path, int tenantId) throws LightRegistryException;

    /**
     * Method to get the resource ID
     *
     * @param path the path to get the path id.
     * @return the resource ID
     * @throws LightRegistryException throws if the operation failed.
     */
    ResourceID getResourceID(String path, int tenantId) throws LightRegistryException;

    /**
     * Method to get the resource ID
     *
     * @param path         the path to get the path id.
     * @param isCollection whether the resource is a collection or not.
     * @return the resource ID
     * @throws LightRegistryException throws if the operation failed.
     */
    ResourceID getResourceID(String path, int tenantId, boolean isCollection) throws LightRegistryException;

    /**
     * Returns the resource in the given path filled with meta-data and access to the content. If a
     * resource does not exist in the given path, null is returned.
     *
     * @param path Path of the resource.
     * @return ResourceImpl filled with resource meta-data and access to the resource content.
     * @throws LightRegistryException throws if the resource retrieval failed.
     */
    ResourceImpl get(String path, int tenantId) throws LightRegistryException;

    /**
     * Add the resource to a path when resource instance and the parent resource id is given.
     *
     * @param path         path of the resource
     * @param parentID     parent resourceID
     * @param resourceImpl the instance of the resource to be added.
     * @throws LightRegistryException throws if the operation failed
     */
    void add(String path, ResourceID parentID, ResourceImpl resourceImpl, int tenantId)
            throws LightRegistryException;

    /**
     * Returns the resource filled with meta-data and access to the content.
     *
     * @param resourceID the resource ID.
     * @return ResourceImpl filled with resource meta-data and access to the resource content.
     * @throws LightRegistryException throws if the resource retrieval failed.
     */
    ResourceImpl getResourceMetaData(int tenantId, ResourceID resourceID) throws LightRegistryException;

    /**
     * Returns the resource filled with meta-data and access to the content.
     *
     * @param path the path of the resource.
     * @return ResourceImpl filled with resource meta-data and access to the resource content.
     * @throws LightRegistryException throws if the resource retrieval failed.
     */
    ResourceImpl getResourceMetaData(int tenantId, String path) throws LightRegistryException;

    /**
     * Fill the resource with the resource object.
     *
     * @param resourceImpl the resource object.
     * @param tenantId     the tenant id.
     * @throws LightRegistryException throws if the operation failed.
     */
    void fillResource(ResourceImpl resourceImpl, int tenantId) throws LightRegistryException;

    /**
     * Fill the properties for a resource.
     *
     * @param tenantId     the tenant id.
     * @param resourceId   the resource object.
     * @param resourceImpl the resource object.
     * @throws LightRegistryException throws if the operation failed.
     */
    void fillResourceProperties(int tenantId, ResourceID resourceId, ResourceImpl resourceImpl)
            throws LightRegistryException;

    /**
     * delete the content for a given content id.
     *
     * @param contentId content id.
     * @throws LightRegistryException throws if the operation failed.
     */
    void deleteContent(int contentId, int tenantId) throws LightRegistryException;

    /**
     * Save the updates of a given resource.
     *
     * @param resourceImpl the resource to be updated.
     * @throws LightRegistryException throws if the operation failed.
     */
    void update(ResourceImpl resourceImpl, int tenantId) throws LightRegistryException;

    /**
     * Fill the children for a resource that is already filled with metadata.
     *
     * @param collection collection to fill the children and properties.
     * @param tenantId   the tenant id.
     * @throws LightRegistryException if the operation failed.
     */
    void fillChildren(CollectionImpl collection, int tenantId) throws LightRegistryException;

    /**
     * Get the children of the collection.
     *
     * @param collection collection to fill the children and properties.
     * @param tenantId   the tenant id.
     * @return an array of children paths.
     * @throws LightRegistryException throws if the operation failed.
     */
    String[] getChildren(CollectionImpl collection, int tenantId) throws LightRegistryException;

    /**
     * Fill resource content for a given resource implementation.
     *
     * @param resourceImpl resource object.
     * @throws LightRegistryException throws if the operation failed.
     */
    void fillResourceContent(int tenantId, ResourceImpl resourceImpl) throws LightRegistryException;

    /**
     * Update the content id of a resource, Normally this should be called after calling
     * addResourceWithoutContentId is called.
     *
     * @param resourceImpl the resource object.
     * @throws LightRegistryException throws if the operation failed.
     */
    void updateContentId(ResourceImpl resourceImpl, int tenantId)
            throws LightRegistryException;

    /**
     * Add a resource without a content id, provided whether it is overwriting existing one or not.
     * If the resource is already existing the removal of the older resource will not be handled in
     * this function.
     *
     * @param resourceImpl the resource object.
     * @throws LightRegistryException throws if the operation failed.
     */
    void addResourceWithoutContentId(ResourceImpl resourceImpl, int tenantId)
            throws LightRegistryException;

    /**
     * Delete the resource provided as a resource DO
     *
     * @param resource the resource to be deleted.
     * @param tenantId the tenant id.
     * @throws LightRegistryException throws if the operation failed.
     */
    void deleteResource(ResourceImpl resource, int tenantId) throws LightRegistryException;

    /**
     * Add the properties to the database from  given resource
     *
     * @param resource to add properties for
     * @throws LightRegistryException throws if the operation failed.
     */
    void addProperties(ResourceImpl resource) throws LightRegistryException;

    /**
     * Remove properties of a resource.
     *
     * @param resource the resource DO which the properties have to be deleted.
     * @param tenantId the tenant id.
     * @throws LightRegistryException throws if the operation failed.
     */
    void removeProperties(ResourceImpl resource, int tenantId) throws LightRegistryException;

    /**
     * Add the content for a resource.
     *
     * @param resourceImpl the resource to add content.
     * @throws LightRegistryException throws if the operation failed.
     */
    void addContent(ResourceImpl resourceImpl, int tenantId) throws LightRegistryException;

    /**
     * Add the content to the content table and return the auto generated id of content table.
     *
     * @param content  the content to be added.
     * @param tenantId the tenant id.
     * @return the auto generated id of content table.
     * @throws LightRegistryException throws if the operation failed.
     */
    int addContentBytes(Object content, int tenantId) throws LightRegistryException;

    /**
     * Get the child path ids of a resource, (should be a collection)
     *
     * @param resourceID the resource id of the collection.
     * @return an array of child path ids.
     * @throws LightRegistryException throws if the operation failed.
     */
    List<ResourceID> getChildPathIds(ResourceID resourceID) throws LightRegistryException;
}
