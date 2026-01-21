/*
 * Krista Ai Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.krista.sdk.kristaai.entity.store;

import app.krista.extension.authorization.AccessDeniedException;
import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extension.util.EntityStore;
import app.krista.extensions.krista.sdk.kristaai.entity.Model;
import app.krista.ksdk.authorization.RoleManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.kristaai.AiModel;
import app.krista.ksdk.kristaai.dto.AiModelDetails;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Store implementation for managing AI Model entities.
 * Provides read-only access to AI models through the Krista AI service.
 * Write operations (create, update, delete) are not supported.
 */
@Service
public class ModelStore implements EntityStore<Model> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelStore.class);

    private final AiModel aiModel;
    private final AuthorizationContext authContext;

    private final RoleManager roleManager;


    @Inject
    public ModelStore(AiModel aiModel, AuthorizationContext authContext, RoleManager roleManager) {
        this.aiModel = aiModel;
        this.authContext = authContext;
        this.roleManager = roleManager;
    }

    /**
     * Retrieves a model by its unique identifier.
     *
     * @param modelId the unique identifier of the model to retrieve
     * @return the Model entity if found, or null if no model exists with the given ID
     * @throws IOException if an error occurs while fetching model data from the AI service
     */
    @Override
    public Model get(String modelId) throws IOException {
        LOGGER.debug("Fetching model with ID: {}", modelId);
        ensureUserHasAccess();
        final Optional<AiModelDetails> aiModelDetails = aiModel.getAllAvailableModels().stream().filter(model -> model.getModelId().equals(modelId)).findFirst();
        Model result = aiModelDetails.map(Model::from).orElse(null);
        if (result == null) {
            LOGGER.warn("Model with ID '{}' not found", modelId);
        }
        return result;
    }

    @Override
    public List<Model> search(List<SearchCondition> list, long l, int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Searches for models based on the provided search query with pagination support.
     * Supports filtering by "Model Name" field using case-insensitive contains matching.
     *
     * @param searchQuery the search query containing filter conditions
     * @param l the page number (1-based)
     * @param i the page size (number of results per page)
     * @return a list of Model entities matching the search criteria for the requested page
     * @throws IOException if an error occurs while fetching model data from the AI service
     */
    @Override
    public List<Model> search(SearchQuery searchQuery, long l, int i) throws IOException {
        try {
            LOGGER.debug("Searching models with query: {}, page: {}, pageSize: {}", searchQuery, l, i);
            ensureUserHasAccess();
            List<Model> allModels = aiModel.getAllAvailableModels().stream().map(Model::from).collect(Collectors.toList());
            List<Model> filteredList = new ArrayList<>();
            for (SearchCondition condition : searchQuery.getSearchConditions()) {
                String searchText = condition.getOperand() != null ? String.valueOf(condition.getOperand()).trim() : "";
                String selector = condition.getFieldName();
                if ("Model Name".equalsIgnoreCase(selector)) {
                    filteredList = searchText.isEmpty() ? allModels : allModels.stream().filter(model -> model.modelName.toLowerCase().contains(searchText.toLowerCase())).collect(Collectors.toList());
                }
            }
            if (searchQuery.getSearchConditions().size() == 1 && filteredList.isEmpty()) {
                filteredList = allModels;
            }
            int startIndex = (int)((l-1)*i);
            int endIndex = Math.min((int) (l * i), filteredList.size());

            LOGGER.debug("Search returned {} models (total filtered: {})", endIndex - startIndex, filteredList.size());
            return filteredList.subList(startIndex, endIndex);
        } catch (IllegalStateException cause) {
            LOGGER.error("Failed to search models", cause);
            throw new IOException(cause);
        }
    }

    @Override
    public Model create(Map<String, Object> map) throws IOException {
        throw new UnsupportedOperationException("Models are read-only and cannot be created through this extension. Models are managed by the Krista AI service.");
    }

    @Override
    public Model update(Model model) throws IOException {
        throw new UnsupportedOperationException("Models are read-only and cannot be updated through this extension. Use the retraining workflow to create new model versions.");
    }

    @Override
    public void delete(String s) throws IOException {
        throw new UnsupportedOperationException("Models are read-only and cannot be deleted through this extension. Model lifecycle is managed by the Krista AI service.");

    }

    /**
     * Checks whether a model with the specified ID exists in the store.
     *
     * @param modelId the unique identifier of the model to check
     * @return true if a model with the given ID exists, false otherwise
     * @throws IOException if an error occurs while checking model existence
     */
    @Override
    public boolean contains(String modelId) throws IOException {
        LOGGER.debug("Checking if model exists with ID: {}", modelId);
        ensureUserHasAccess();
        boolean exists = aiModel.getAllAvailableModels().stream().anyMatch(model -> model.getModelId().equals(modelId));
        LOGGER.debug("Model with ID '{}' exists: {}", modelId, exists);
        return exists;
    }

    @Override
    public long count(List<SearchCondition> list) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> lookup(Map<String, Object> map, List<String> list) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Verifies that the current user has workspace admin access.
     * This method is called before any data access operation to enforce authorization.
     *
     * @throws AccessDeniedException if the user is not a workspace admin
     * @throws UnsupportedOperationException if authorization check fails due to an unexpected error
     */
    private void ensureUserHasAccess()  {
        try {
            if (!roleManager.isWorkspaceAdmin(authContext.getAuthorizedAccount())) {
                LOGGER.warn("Access denied: User is not a workspace admin");
                throw new AccessDeniedException();
            }
        } catch (Exception cause) {
            LOGGER.error("Authorization check failed: {}", cause.getMessage(), cause);
            throw new UnsupportedOperationException("Access denied. You must be a workspace admin to access AI models. Please contact your administrator if you need access.");
        }
    }
}
