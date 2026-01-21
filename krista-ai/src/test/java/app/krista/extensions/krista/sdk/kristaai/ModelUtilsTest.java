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

package app.krista.extensions.krista.sdk.kristaai;

import app.krista.extensions.krista.sdk.kristaai.entity.store.ModelStore;
import app.krista.extensions.krista.sdk.kristaai.util.ModelUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModelUtilsTest {

    @Mock
    ModelStore modelStore;

    @Test
    public void testValidate_ModelExists() throws IOException {
        String existingModelId = "existing-model";
        when(modelStore.contains(existingModelId)).thenReturn(true);

        // No assertion here - we expect no exception to be thrown
        ModelUtils.validateModelExistsAndAccessibleOrThrowError(modelStore, existingModelId);
    }

    @Test
    public void testValidate_ModelDoesNotExist() throws IOException {
        String missingModelId = "unknown-model";
        when(modelStore.contains(missingModelId)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                ModelUtils.validateModelExistsAndAccessibleOrThrowError(modelStore, missingModelId)
        );
        assertEquals("The specified model does not exist. Please verify the Model ID using entity selection or check that the model has not been deleted.", exception.getMessage());
    }

    @Test
    public void testValidate_IOException() throws IOException {
        String someModelId = "any-model";
        when(modelStore.contains(someModelId)).thenThrow(new IOException("Test IO Error"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                ModelUtils.validateModelExistsAndAccessibleOrThrowError(modelStore, someModelId)
        );
        assertEquals("Unable to fetch model information. Please try again later.", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(IOException.class, exception.getCause().getClass());
    }
}
