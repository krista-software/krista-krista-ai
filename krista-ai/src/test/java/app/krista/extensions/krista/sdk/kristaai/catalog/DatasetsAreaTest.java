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
import app.krista.ksdk.kristaai.AiDataset;
import app.krista.model.base.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DatasetsAreaTest {

    @Mock
    private AiDataset aiDataset;

    @Mock
    private ModelStore modelStore;

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private TelemetryHelper.TelemetryOperation telemetryOperation;

    private DatasetsArea datasetsArea;

    @BeforeEach
    public void setUp() {
        when(telemetryHelper.startOperation(anyString())).thenReturn(telemetryOperation);
        datasetsArea = new DatasetsArea(aiDataset, modelStore, telemetryHelper);
    }

    @Test
    public void testUploadDataForRetraining_Success() throws Exception {
        // Arrange
        String modelId = "model-123";
        File file = mock(File.class);

        when(modelStore.contains(modelId)).thenReturn(true);
        doNothing().when(aiDataset).uploadDataToStaging(modelId, file);

        // Act
        Map<String, Object> result = datasetsArea.uploadDataForRetraining(modelId, file);

        // Assert
        assertFalse((Boolean) result.get("Error"));
        assertEquals("Dataset uploaded successfully", result.get("Message"));
        assertEquals("Success", result.get("Status"));

        verify(modelStore).contains(modelId);
        verify(aiDataset).uploadDataToStaging(modelId, file);
    }

    @Test
    public void testUploadDataForRetraining_ModelDoesNotExist() throws Exception {
        // Arrange
        String modelId = "non-existent-model";
        File file = mock(File.class);
        
        when(modelStore.contains(modelId)).thenReturn(false);

        // Act
        Map<String, Object> result = datasetsArea.uploadDataForRetraining(modelId, file);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("does not exist"));
        assertEquals("Failed", result.get("Status"));

        verify(modelStore).contains(modelId);
        verify(aiDataset, never()).uploadDataToStaging(anyString(), any(File.class));
    }

    @Test
    public void testUploadDataForRetraining_IOExceptionDuringValidation() throws Exception {
        // Arrange
        String modelId = "model-123";
        File file = mock(File.class);
        
        when(modelStore.contains(modelId)).thenThrow(new IOException("Database error"));

        // Act
        Map<String, Object> result = datasetsArea.uploadDataForRetraining(modelId, file);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("fetch model information"));
        assertEquals("Failed", result.get("Status"));
    }

    @Test
    public void testUploadDataForRetraining_RuntimeException() throws Exception {
        // Arrange
        String modelId = "model-123";
        File file = mock(File.class);
        
        when(modelStore.contains(modelId)).thenReturn(true);
        doThrow(new RuntimeException("Upload failed")).when(aiDataset).uploadDataToStaging(modelId, file);

        // Act
        Map<String, Object> result = datasetsArea.uploadDataForRetraining(modelId, file);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("unexpected error"));
        assertEquals("Failed", result.get("Status"));
    }

    @Test
    public void testUploadDataForRetraining_NullFile() throws Exception {
        // Arrange
        String modelId = "model-123";
        
        when(modelStore.contains(modelId)).thenReturn(true);
        doThrow(new RuntimeException("File cannot be null")).when(aiDataset).uploadDataToStaging(eq(modelId), isNull());

        // Act
        Map<String, Object> result = datasetsArea.uploadDataForRetraining(modelId, null);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertEquals("Failed", result.get("Status"));
    }
}

