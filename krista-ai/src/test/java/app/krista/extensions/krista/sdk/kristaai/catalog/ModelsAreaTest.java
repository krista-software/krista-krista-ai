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

package app.krista.extensions.krista.sdk.kristaai.catalog;

import app.krista.extensions.krista.sdk.kristaai.entity.store.ModelStore;
import app.krista.extensions.krista.sdk.kristaai.util.TelemetryHelper;
import app.krista.ksdk.kristaai.AiModel;
import app.krista.ksdk.kristaai.dto.AiModelVersionDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModelsAreaTest {

    @Mock
    private ModelStore modelStore;

    @Mock
    private AiModel aiModel;

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private TelemetryHelper.TelemetryOperation telemetryOperation;

    private ModelsArea modelsArea;

    @BeforeEach
    public void setUp() {
        when(telemetryHelper.startOperation(anyString())).thenReturn(telemetryOperation);
        modelsArea = new ModelsArea(modelStore, aiModel, telemetryHelper);
    }

    @Test
    public void testListModelVersions_Success() throws Exception {
        // Arrange
        String modelId = "model-123";
        when(modelStore.contains(modelId)).thenReturn(true);

        AiModelVersionDetails version1 = mock(AiModelVersionDetails.class);
        when(version1.getVersion()).thenReturn("v1.0");
        when(version1.isLive()).thenReturn(true);
        when(version1.getAccuracy()).thenReturn(0.95f);
        when(version1.getCreated()).thenReturn(1000L);

        AiModelVersionDetails version2 = mock(AiModelVersionDetails.class);
        when(version2.getVersion()).thenReturn("v2.0");
        when(version2.isLive()).thenReturn(false);
        when(version2.getAccuracy()).thenReturn(0.97f);
        when(version2.getCreated()).thenReturn(2000L);

        when(aiModel.getModelVersions(modelId)).thenReturn(Arrays.asList(version1, version2));

        // Act
        Map<String, Object> result = modelsArea.listModelVersions(modelId);

        // Assert
        assertFalse((Boolean) result.get("Error"));
        assertEquals("", result.get("Message"));
        
        @SuppressWarnings("unchecked")
        List<String> versionNames = (List<String>) result.get("Version Names");
        assertEquals(2, versionNames.size());
        assertTrue(versionNames.contains("v1.0"));
        assertTrue(versionNames.contains("v2.0"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> versionStats = (List<Map<String, Object>>) result.get("Version Stats");
        assertEquals(2, versionStats.size());
        
        verify(modelStore).contains(modelId);
        verify(aiModel).getModelVersions(modelId);
    }

    @Test
    public void testListModelVersions_ModelDoesNotExist() throws Exception {
        // Arrange
        String modelId = "non-existent-model";
        when(modelStore.contains(modelId)).thenReturn(false);

        // Act
        Map<String, Object> result = modelsArea.listModelVersions(modelId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("does not exist"));
        
        verify(modelStore).contains(modelId);
        verify(aiModel, never()).getModelVersions(anyString());
    }

    @Test
    public void testListModelVersions_IOExceptionDuringValidation() throws Exception {
        // Arrange
        String modelId = "model-123";
        when(modelStore.contains(modelId)).thenThrow(new IOException("Database error"));

        // Act
        Map<String, Object> result = modelsArea.listModelVersions(modelId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("fetch model information"));
    }

    @Test
    public void testListModelVersions_RuntimeException() throws Exception {
        // Arrange
        String modelId = "model-123";
        when(modelStore.contains(modelId)).thenReturn(true);
        when(aiModel.getModelVersions(modelId)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        Map<String, Object> result = modelsArea.listModelVersions(modelId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("unexpected error"));
    }

    @Test
    public void testChangeLiveVersion_Success() throws Exception {
        // Arrange
        String modelId = "model-123";
        String targetVersion = "v2.0";
        when(modelStore.contains(modelId)).thenReturn(true);
        doNothing().when(aiModel).activateModelVersion(modelId, targetVersion);

        // Act
        Map<String, Object> result = modelsArea.changeLiveVersion(modelId, targetVersion);

        // Assert
        assertFalse((Boolean) result.get("Error"));
        assertEquals("Live version changed successfully!", result.get("Message"));
        
        verify(modelStore).contains(modelId);
        verify(aiModel).activateModelVersion(modelId, targetVersion);
    }

    @Test
    public void testChangeLiveVersion_ModelDoesNotExist() throws Exception {
        // Arrange
        String modelId = "non-existent-model";
        String targetVersion = "v2.0";
        when(modelStore.contains(modelId)).thenReturn(false);

        // Act
        Map<String, Object> result = modelsArea.changeLiveVersion(modelId, targetVersion);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("does not exist"));
        
        verify(modelStore).contains(modelId);
        verify(aiModel, never()).activateModelVersion(anyString(), anyString());
    }

    @Test
    public void testChangeLiveVersion_IOExceptionDuringValidation() throws Exception {
        // Arrange
        String modelId = "model-123";
        String targetVersion = "v2.0";
        when(modelStore.contains(modelId)).thenThrow(new IOException("Database error"));

        // Act
        Map<String, Object> result = modelsArea.changeLiveVersion(modelId, targetVersion);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("fetch model information"));
    }

    @Test
    public void testChangeLiveVersion_RuntimeException() throws Exception {
        // Arrange
        String modelId = "model-123";
        String targetVersion = "v2.0";
        when(modelStore.contains(modelId)).thenReturn(true);
        doThrow(new RuntimeException("Activation failed")).when(aiModel).activateModelVersion(modelId, targetVersion);

        // Act
        Map<String, Object> result = modelsArea.changeLiveVersion(modelId, targetVersion);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("unexpected error"));
    }
}

