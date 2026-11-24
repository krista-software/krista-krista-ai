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
import app.krista.extensions.krista.sdk.kristaai.entity.Model;
import app.krista.ksdk.authorization.RoleManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.kristaai.AiModel;
import app.krista.ksdk.kristaai.dto.AiModelDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModelStoreTest {

    @Mock
    private AiModel aiModel;

    @Mock
    private AuthorizationContext authContext;

    @Mock
    private RoleManager roleManager;

    private ModelStore modelStore;

    @BeforeEach
    public void setUp() {
        modelStore = new ModelStore(aiModel, authContext, roleManager);
    }

    @Test
    public void testGet_Success() throws Exception {
        // Arrange
        String modelId = "model-123";
        AiModelDetails modelDetails = mock(AiModelDetails.class);
        when(modelDetails.getModelId()).thenReturn(modelId);
        when(modelDetails.getModelName()).thenReturn("Test Model");
        when(modelDetails.getModelCreationTime()).thenReturn(1234567890L);

        when(roleManager.isWorkspaceAdmin(any())).thenReturn(true);
        when(aiModel.getAllAvailableModels()).thenReturn(Arrays.asList(modelDetails));

        // Act
        Model result = modelStore.get(modelId);

        // Assert
        assertNotNull(result);
        assertEquals(modelId, result.modelId);
        assertEquals("Test Model", result.modelName);
        assertEquals(1234567890L, result.creationTime);
        verify(roleManager).isWorkspaceAdmin(any());
        verify(aiModel).getAllAvailableModels();
    }

    @Test
    public void testGet_ModelNotFound() throws Exception {
        // Arrange
        String modelId = "non-existent-model";
        AiModelDetails modelDetails = mock(AiModelDetails.class);
        when(modelDetails.getModelId()).thenReturn("different-model");

        when(roleManager.isWorkspaceAdmin(any())).thenReturn(true);
        when(aiModel.getAllAvailableModels()).thenReturn(Arrays.asList(modelDetails));

        // Act
        Model result = modelStore.get(modelId);

        // Assert
        assertNull(result);
    }

    @Test
    public void testGet_AccessDenied() throws Exception {
        // Arrange
        when(roleManager.isWorkspaceAdmin(any())).thenReturn(false);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> modelStore.get("model-123"));
    }

    @Test
    public void testSearch_WithModelNameFilter() throws Exception {
        // Arrange
        AiModelDetails model1 = mock(AiModelDetails.class);
        when(model1.getModelId()).thenReturn("model-1");
        when(model1.getModelName()).thenReturn("Classification Model");
        when(model1.getModelCreationTime()).thenReturn(1000L);

        AiModelDetails model2 = mock(AiModelDetails.class);
        when(model2.getModelId()).thenReturn("model-2");
        when(model2.getModelName()).thenReturn("Regression Model");
        when(model2.getModelCreationTime()).thenReturn(2000L);

        when(roleManager.isWorkspaceAdmin(any())).thenReturn(true);
        when(aiModel.getAllAvailableModels()).thenReturn(Arrays.asList(model1, model2));

        SearchCondition condition = mock(SearchCondition.class);
        when(condition.getFieldName()).thenReturn("Model Name");
        when(condition.getOperand()).thenReturn("Classification");

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getSearchConditions()).thenReturn(Arrays.asList(condition));

        // Act
        List<Model> results = modelStore.search(searchQuery, 1, 10);

        // Assert
        assertEquals(1, results.size());
        assertEquals("model-1", results.get(0).modelId);
        assertEquals("Classification Model", results.get(0).modelName);
    }

    @Test
    public void testSearch_EmptySearchReturnsAll() throws Exception {
        // Arrange
        AiModelDetails model1 = mock(AiModelDetails.class);
        when(model1.getModelId()).thenReturn("model-1");
        when(model1.getModelName()).thenReturn("Model 1");
        when(model1.getModelCreationTime()).thenReturn(1000L);

        AiModelDetails model2 = mock(AiModelDetails.class);
        when(model2.getModelId()).thenReturn("model-2");
        when(model2.getModelName()).thenReturn("Model 2");
        when(model2.getModelCreationTime()).thenReturn(2000L);

        when(roleManager.isWorkspaceAdmin(any())).thenReturn(true);
        when(aiModel.getAllAvailableModels()).thenReturn(Arrays.asList(model1, model2));

        SearchCondition condition = mock(SearchCondition.class);
        when(condition.getFieldName()).thenReturn("Model Name");
        when(condition.getOperand()).thenReturn("");

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getSearchConditions()).thenReturn(Arrays.asList(condition));

        // Act
        List<Model> results = modelStore.search(searchQuery, 1, 10);

        // Assert
        assertEquals(2, results.size());
    }

    @Test
    public void testSearch_Pagination() throws Exception {
        // Arrange
        AiModelDetails model1 = mock(AiModelDetails.class);
        when(model1.getModelId()).thenReturn("model-1");
        when(model1.getModelName()).thenReturn("Model 1");
        when(model1.getModelCreationTime()).thenReturn(1000L);

        AiModelDetails model2 = mock(AiModelDetails.class);
        when(model2.getModelId()).thenReturn("model-2");
        when(model2.getModelName()).thenReturn("Model 2");
        when(model2.getModelCreationTime()).thenReturn(2000L);

        AiModelDetails model3 = mock(AiModelDetails.class);
        when(model3.getModelId()).thenReturn("model-3");
        when(model3.getModelName()).thenReturn("Model 3");
        when(model3.getModelCreationTime()).thenReturn(3000L);

        when(roleManager.isWorkspaceAdmin(any())).thenReturn(true);
        when(aiModel.getAllAvailableModels()).thenReturn(Arrays.asList(model1, model2, model3));

        SearchCondition condition = mock(SearchCondition.class);
        when(condition.getFieldName()).thenReturn("Model Name");
        when(condition.getOperand()).thenReturn("");

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getSearchConditions()).thenReturn(Arrays.asList(condition));

        // Act - Get page 2 with page size 2
        List<Model> results = modelStore.search(searchQuery, 2, 2);

        // Assert
        assertEquals(1, results.size());
        assertEquals("model-3", results.get(0).modelId);
    }

    @Test
    public void testSearch_AccessDenied() {
        // Arrange
        when(roleManager.isWorkspaceAdmin(any())).thenReturn(false);
        SearchQuery searchQuery = mock(SearchQuery.class);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> modelStore.search(searchQuery, 1, 10));
    }

    @Test
    public void testContains_ModelExists() throws Exception {
        // Arrange
        String modelId = "model-123";
        AiModelDetails modelDetails = mock(AiModelDetails.class);
        when(modelDetails.getModelId()).thenReturn(modelId);

        when(roleManager.isWorkspaceAdmin(any())).thenReturn(true);
        when(aiModel.getAllAvailableModels()).thenReturn(Arrays.asList(modelDetails));

        // Act
        boolean result = modelStore.contains(modelId);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testContains_ModelDoesNotExist() throws Exception {
        // Arrange
        AiModelDetails modelDetails = mock(AiModelDetails.class);
        when(modelDetails.getModelId()).thenReturn("different-model");

        when(roleManager.isWorkspaceAdmin(any())).thenReturn(true);
        when(aiModel.getAllAvailableModels()).thenReturn(Arrays.asList(modelDetails));

        // Act
        boolean result = modelStore.contains("non-existent-model");

        // Assert
        assertFalse(result);
    }

    @Test
    public void testContains_AccessDenied() {
        // Arrange
        when(roleManager.isWorkspaceAdmin(any())).thenReturn(false);

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> modelStore.contains("model-123"));
    }

    @Test
    public void testCreate_ThrowsUnsupportedOperation() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> modelStore.create(null));
    }

    @Test
    public void testUpdate_ThrowsUnsupportedOperation() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> modelStore.update(null));
    }

    @Test
    public void testDelete_ThrowsUnsupportedOperation() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> modelStore.delete("model-123"));
    }

    @Test
    public void testCount_ThrowsUnsupportedOperation() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> modelStore.count(null));
    }

    @Test
    public void testLookup_ThrowsUnsupportedOperation() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> modelStore.lookup(null, null));
    }
}

