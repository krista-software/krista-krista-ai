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
import app.krista.ksdk.kristaai.AiModelStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingAreaTest {

    @Mock
    private AiModel aiModel;

    @Mock
    private ModelStore modelStore;

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private TelemetryHelper.TelemetryOperation telemetryOperation;

    private TrainingArea trainingArea;

    @BeforeEach
    public void setUp() {
        when(telemetryHelper.startOperation(anyString())).thenReturn(telemetryOperation);
        trainingArea = new TrainingArea(aiModel, modelStore, telemetryHelper);
    }

    @Test
    public void testRetrainModel_Success() throws Exception {
        // Arrange
        String modelId = "model-123";
        String jobId = "job-456";
        when(modelStore.contains(modelId)).thenReturn(true);
        when(aiModel.retrainModel(modelId)).thenReturn(jobId);

        // Act
        Map<String, Object> result = trainingArea.retrainModel(modelId);

        // Assert
        assertFalse((Boolean) result.get("Error"));
        assertEquals("Training job queued successfully!", result.get("Message"));
        assertEquals(jobId, result.get("Retraining Job Id"));
        
        verify(modelStore).contains(modelId);
        verify(aiModel).retrainModel(modelId);
    }

    @Test
    public void testRetrainModel_ModelDoesNotExist() throws Exception {
        // Arrange
        String modelId = "non-existent-model";
        when(modelStore.contains(modelId)).thenReturn(false);

        // Act
        Map<String, Object> result = trainingArea.retrainModel(modelId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("does not exist"));
        assertEquals("", result.get("Retraining Job Id"));
        
        verify(modelStore).contains(modelId);
        verify(aiModel, never()).retrainModel(anyString());
    }

    @Test
    public void testRetrainModel_IOExceptionDuringValidation() throws Exception {
        // Arrange
        String modelId = "model-123";
        when(modelStore.contains(modelId)).thenThrow(new IOException("Database error"));

        // Act
        Map<String, Object> result = trainingArea.retrainModel(modelId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("fetch model information"));
        assertEquals("", result.get("Retraining Job Id"));
    }

    @Test
    public void testRetrainModel_RuntimeException() throws Exception {
        // Arrange
        String modelId = "model-123";
        when(modelStore.contains(modelId)).thenReturn(true);
        when(aiModel.retrainModel(modelId)).thenThrow(new RuntimeException("Training failed"));

        // Act
        Map<String, Object> result = trainingArea.retrainModel(modelId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("unexpected error"));
        assertEquals("", result.get("Retraining Job Id"));
    }

    @Test
    public void testRetrainingStatus_Success() throws Exception {
        // Arrange
        String modelId = "model-123";
        String jobId = "job-456";
        AiModelStatus mockStatus = mock(AiModelStatus.class);
        when(mockStatus.name()).thenReturn("COMPLETED");

        when(modelStore.contains(modelId)).thenReturn(true);
        when(aiModel.getRetrainingStatus(modelId, jobId)).thenReturn(mockStatus);

        // Act
        Map<String, Object> result = trainingArea.retrainingStatus(modelId, jobId);

        // Assert
        assertFalse((Boolean) result.get("Error"));
        assertEquals("", result.get("Message"));
        assertEquals("COMPLETED", result.get("Status"));

        verify(modelStore).contains(modelId);
        verify(aiModel).getRetrainingStatus(modelId, jobId);
    }

    @Test
    public void testRetrainingStatus_InProgress() throws Exception {
        // Arrange
        String modelId = "model-123";
        String jobId = "job-456";
        AiModelStatus mockStatus = mock(AiModelStatus.class);
        when(mockStatus.name()).thenReturn("IN_PROGRESS");

        when(modelStore.contains(modelId)).thenReturn(true);
        when(aiModel.getRetrainingStatus(modelId, jobId)).thenReturn(mockStatus);

        // Act
        Map<String, Object> result = trainingArea.retrainingStatus(modelId, jobId);

        // Assert
        assertFalse((Boolean) result.get("Error"));
        assertEquals("IN_PROGRESS", result.get("Status"));
    }

    @Test
    public void testRetrainingStatus_Failed() throws Exception {
        // Arrange
        String modelId = "model-123";
        String jobId = "job-456";
        AiModelStatus mockStatus = mock(AiModelStatus.class);
        when(mockStatus.name()).thenReturn("FAILED");

        when(modelStore.contains(modelId)).thenReturn(true);
        when(aiModel.getRetrainingStatus(modelId, jobId)).thenReturn(mockStatus);

        // Act
        Map<String, Object> result = trainingArea.retrainingStatus(modelId, jobId);

        // Assert
        assertFalse((Boolean) result.get("Error"));
        assertEquals("FAILED", result.get("Status"));
    }

    @Test
    public void testRetrainingStatus_ModelDoesNotExist() throws Exception {
        // Arrange
        String modelId = "non-existent-model";
        String jobId = "job-456";
        when(modelStore.contains(modelId)).thenReturn(false);

        // Act
        Map<String, Object> result = trainingArea.retrainingStatus(modelId, jobId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("does not exist"));
        assertEquals("Unknown", result.get("Status"));
        
        verify(modelStore).contains(modelId);
        verify(aiModel, never()).getRetrainingStatus(anyString(), anyString());
    }

    @Test
    public void testRetrainingStatus_IOExceptionDuringValidation() throws Exception {
        // Arrange
        String modelId = "model-123";
        String jobId = "job-456";
        when(modelStore.contains(modelId)).thenThrow(new IOException("Database error"));

        // Act
        Map<String, Object> result = trainingArea.retrainingStatus(modelId, jobId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("fetch model information"));
        assertEquals("Unknown", result.get("Status"));
    }

    @Test
    public void testRetrainingStatus_RuntimeException() throws Exception {
        // Arrange
        String modelId = "model-123";
        String jobId = "job-456";
        when(modelStore.contains(modelId)).thenReturn(true);
        when(aiModel.getRetrainingStatus(modelId, jobId)).thenThrow(new RuntimeException("Status check failed"));

        // Act
        Map<String, Object> result = trainingArea.retrainingStatus(modelId, jobId);

        // Assert
        assertTrue((Boolean) result.get("Error"));
        assertNotNull(result.get("Message"));
        assertTrue(result.get("Message").toString().contains("unexpected error"));
        assertEquals("Unknown", result.get("Status"));
    }
}

