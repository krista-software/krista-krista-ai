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

package app.krista.extensions.krista.sdk.kristaai.util;

import app.krista.extensions.krista.sdk.kristaai.entity.store.ModelStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utility class for model-related validation operations.
 */
public class ModelUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

    /**
     * Validates that the model with the given modelId exists and is accessible in the given modelStore.
     *
     * @param modelStore the model store containing the models
     * @param modelId the id of the model to check
     * @throws IllegalStateException if the model does not exist in the model store
     * @throws IllegalStateException if there is an IOException while checking if the model store contains the model
     */
    public static void validateModelExistsAndAccessibleOrThrowError(ModelStore modelStore, String modelId) {
        try {
            if (!modelStore.contains(modelId)){
                LOGGER.error("Model validation failed: Model with ID '{}' does not exist", modelId);
                throw new IllegalStateException("The specified model does not exist. Please verify the Model ID using entity selection or check that the model has not been deleted.");
            }
        } catch (IOException cause) {
            LOGGER.error("Failed to fetch model information for model ID: {}", modelId, cause);
            throw new IllegalStateException("Unable to fetch model information. Please try again later.", cause);
        }
    }
}
