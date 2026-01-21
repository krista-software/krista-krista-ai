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

package app.krista.extensions.krista.sdk.kristaai.entity;

import app.krista.ksdk.kristaai.dto.AiModelDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ModelTest {

    @Test
    public void testFrom_Success() {
        // Arrange
        AiModelDetails aiModelDetails = mock(AiModelDetails.class);
        when(aiModelDetails.getModelId()).thenReturn("model-123");
        when(aiModelDetails.getModelName()).thenReturn("Test Model");
        when(aiModelDetails.getModelCreationTime()).thenReturn(1234567890L);

        // Act
        Model model = Model.from(aiModelDetails);

        // Assert
        assertNotNull(model);
        assertEquals("model-123", model.modelId);
        assertEquals("Test Model", model.modelName);
        assertEquals(1234567890L, model.creationTime);
        
        verify(aiModelDetails).getModelId();
        verify(aiModelDetails).getModelName();
        verify(aiModelDetails).getModelCreationTime();
    }

    @Test
    public void testFrom_WithNullCreationTime() {
        // Arrange
        AiModelDetails aiModelDetails = mock(AiModelDetails.class);
        when(aiModelDetails.getModelId()).thenReturn("model-456");
        when(aiModelDetails.getModelName()).thenReturn("Another Model");
        when(aiModelDetails.getModelCreationTime()).thenReturn(null);

        // Act
        Model model = Model.from(aiModelDetails);

        // Assert
        assertNotNull(model);
        assertEquals("model-456", model.modelId);
        assertEquals("Another Model", model.modelName);
        assertNull(model.creationTime);
    }

    @Test
    public void testFrom_WithEmptyModelName() {
        // Arrange
        AiModelDetails aiModelDetails = mock(AiModelDetails.class);
        when(aiModelDetails.getModelId()).thenReturn("model-789");
        when(aiModelDetails.getModelName()).thenReturn("");
        when(aiModelDetails.getModelCreationTime()).thenReturn(9876543210L);

        // Act
        Model model = Model.from(aiModelDetails);

        // Assert
        assertNotNull(model);
        assertEquals("model-789", model.modelId);
        assertEquals("", model.modelName);
        assertEquals(9876543210L, model.creationTime);
    }

    @Test
    public void testModelFields_ArePublic() {
        // Arrange & Act
        Model model = new Model();
        model.modelId = "test-id";
        model.modelName = "test-name";
        model.creationTime = 12345L;

        // Assert
        assertEquals("test-id", model.modelId);
        assertEquals("test-name", model.modelName);
        assertEquals(12345L, model.creationTime);
    }
}

