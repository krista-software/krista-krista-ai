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

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.krista.sdk.kristaai.entity.store.ModelStore;
import app.krista.extensions.krista.sdk.kristaai.util.TelemetryHelper;
import app.krista.ksdk.kristaai.AiModel;
import app.krista.ksdk.kristaai.AiModelStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static app.krista.extensions.krista.sdk.kristaai.util.AiResponseKeys.*;
import static app.krista.extensions.krista.sdk.kristaai.util.ModelUtils.validateModelExistsAndAccessibleOrThrowError;

@Domain(id = "catEntryDomain_c2394eb0-02d9-4916-a3dc-909b1c631ea0", name = "SDK", ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9", ecosystemName = "Krista", ecosystemVersion = "f11e6c89-11b1-4264-ac9c-a7f174fe1e6c")
public class TrainingArea {
    public static final String STATUS_UNKNOWN = "Unknown";
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingArea.class);
    private final AiModel aiModel;
    private final ModelStore modelStore;
    private final TelemetryHelper telemetryHelper;

    @Inject
    public TrainingArea(AiModel aiModel, ModelStore modelStore, TelemetryHelper telemetryHelper) {
        this.aiModel = aiModel;
        this.modelStore = modelStore;
        this.telemetryHelper = telemetryHelper;
    }

    /**
     * Initiates a retraining job for the specified AI model.
     * Retraining requires that new data has been previously staged using the upload data operation.
     *
     * @param modelId the unique identifier of the model to retrain
     * @return a map containing the retraining job ID and status with keys: Retraining Job Id (string),
     *         ERROR (boolean), MESSAGE (string)
     */
    @CatalogRequest(id = "localDomainRequest_4213fd04-d991-4ba7-b0a6-0a1d3c212094", name = "Retrain Model", description = "Any available AI-model managed by Krista-AI can be retrained. Retraining request will be rejected if no new data is staged or uploaded", area = "Training", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = RETRAINING_JOB_ID, type = "Identifier", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Boolean(name = ERROR, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = MESSAGE, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Map<String, Object> retrainModel(
            @Field(name = "Model Id", type = "Identifier", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String modelId) {

        TelemetryHelper.TelemetryOperation operation = telemetryHelper.startOperation("retrainModel");

        try {
            LOGGER.info("Starting retraining for model ID: {}", modelId);
            validateModelExistsAndAccessibleOrThrowError(modelStore, modelId);

            String jobId = this.aiModel.retrainModel(modelId);
            LOGGER.info("Retraining job queued successfully for model ID '{}' with job ID: {}", modelId, jobId);
            operation.recordSuccess();
            return Map.of(RETRAINING_JOB_ID, jobId, ERROR, false, MESSAGE, "Training job queued successfully!");
        } catch (IllegalStateException cause) {
            LOGGER.error("Failed to retrain model ID '{}': {}", modelId, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of(RETRAINING_JOB_ID, "", ERROR, true, MESSAGE, cause.getMessage() == null ? "" : cause.getMessage());
        } catch (RuntimeException cause) {
            LOGGER.error("Unexpected error while retraining model ID '{}': {}", modelId, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of(RETRAINING_JOB_ID, "", ERROR, true, MESSAGE, "An unexpected error occurred while starting the retraining job. " + (cause.getMessage() == null ? "" : cause.getMessage()));
        }
    }

    /**
     * Retrieves the current status of a retraining job.
     *
     * @param modelId the unique identifier of the model being retrained
     * @param retrainingJobId the unique identifier of the retraining job to check
     * @return a map containing the job status with keys: STATUS (string), ERROR (boolean), MESSAGE (string)
     */
    @CatalogRequest(id = "localDomainRequest_dff074ed-f741-4a8c-b1fe-cda94b7ded55", name = "Get Retraining Status", description = "Fetches the status of a retraining job", area = "Training", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = STATUS, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Boolean(name = ERROR, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = MESSAGE, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Map<String, Object> retrainingStatus(
            @Field(name = "Model Id", type = "Identifier", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String modelId,
            @Field(name = "Retraining Job Id", type = "Identifier", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String retrainingJobId) {

        TelemetryHelper.TelemetryOperation operation = telemetryHelper.startOperation("retrainingStatus");

        try {
            LOGGER.info("Fetching retraining status for model ID '{}' and job ID '{}'", modelId, retrainingJobId);
            validateModelExistsAndAccessibleOrThrowError(modelStore, modelId);

            AiModelStatus retrainingStatus = this.aiModel.getRetrainingStatus(modelId, retrainingJobId);
            LOGGER.info("Retraining status for model ID '{}' and job ID '{}': {}", modelId, retrainingJobId, retrainingStatus.name());
            operation.recordSuccess();
            return Map.of(STATUS, retrainingStatus.name(), ERROR, false, MESSAGE, "");
        } catch (IllegalStateException cause) {
            LOGGER.error("Failed to fetch retraining status for model ID '{}' and job ID '{}': {}", modelId, retrainingJobId, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of(STATUS, STATUS_UNKNOWN, ERROR, true, MESSAGE, cause.getMessage() == null ? "" : cause.getMessage());
        } catch (RuntimeException cause) {
            LOGGER.error("Unexpected error while fetching retraining status for model ID '{}' and job ID '{}': {}", modelId, retrainingJobId, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of(STATUS, STATUS_UNKNOWN, ERROR, true, MESSAGE, "An unexpected error occurred while fetching the retraining status. " + (cause.getMessage() == null ? "" : cause.getMessage()));
        }
    }

}