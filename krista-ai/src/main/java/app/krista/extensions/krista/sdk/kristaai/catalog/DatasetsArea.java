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
import app.krista.ksdk.kristaai.AiDataset;
import app.krista.model.base.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static app.krista.extensions.krista.sdk.kristaai.util.AiResponseKeys.ERROR;
import static app.krista.extensions.krista.sdk.kristaai.util.AiResponseKeys.MESSAGE;
import static app.krista.extensions.krista.sdk.kristaai.util.AiResponseKeys.STATUS;
import static app.krista.extensions.krista.sdk.kristaai.util.ModelUtils.validateModelExistsAndAccessibleOrThrowError;

@Domain(id = "catEntryDomain_c2394eb0-02d9-4916-a3dc-909b1c631ea0",
        name = "SDK",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "f11e6c89-11b1-4264-ac9c-a7f174fe1e6c")
public class DatasetsArea {
    private final AiDataset aiDataset;
    private final ModelStore modelStore;
    private final TelemetryHelper telemetryHelper;
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetsArea.class);

    @Inject
    public DatasetsArea(AiDataset aiDatasets, ModelStore modelStore, TelemetryHelper telemetryHelper) {
        this.aiDataset = aiDatasets;
        this.modelStore = modelStore;
        this.telemetryHelper = telemetryHelper;
    }

    /**
     * Uploads a dataset file to the staging area for model retraining.
     * The uploaded data will be used when the retraining process is triggered.
     *
     * @param modelId the unique identifier of the model to upload data for
     * @param file the dataset file to upload for retraining
     * @return a map containing operation status with keys: ERROR (boolean), MESSAGE (string), STATUS (string)
     */
    @CatalogRequest(
            id = "localDomainRequest_7ce3e4fa-a3cd-46fe-b1a7-be9d91c81119",
            name = "Upload Data for Retraining",
            description = "Stages new data for retraining of a specific model. If no data is staged, the retraining won't be triggered by the backend.",
            area = "Datasets",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = ERROR, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = MESSAGE, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = STATUS, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Map<String, Object> uploadDataForRetraining(
            @Field(name = "Model Id", type = "Identifier", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String modelId,
            @Field.File(name = "File", multipleFileUpload = false, required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) File file) {

        TelemetryHelper.TelemetryOperation operation = telemetryHelper.startOperation("uploadDataForRetraining");

        try {
            LOGGER.info("Uploading dataset for model ID: {}", modelId);
            validateModelExistsAndAccessibleOrThrowError(modelStore, modelId);
            this.aiDataset.uploadDataToStaging(modelId, file);
            LOGGER.info("Dataset uploaded successfully for model ID: {}", modelId);
            operation.recordSuccess();
            return Map.of(ERROR, false, MESSAGE, "Dataset uploaded successfully", STATUS, "Success");
        } catch (IllegalStateException cause) {
            LOGGER.error("Failed to upload dataset for model ID '{}': {}", modelId, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of(ERROR, true, MESSAGE, cause.getMessage() == null ? "" : cause.getMessage(), STATUS, "Failed");
        } catch (RuntimeException cause) {
            LOGGER.error("Unexpected error while uploading dataset for model ID '{}': {}", modelId, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of(ERROR, true, MESSAGE, "An unexpected error occurred while uploading the dataset. " + (cause.getMessage() == null ? "" : cause.getMessage()), STATUS, "Failed");
        }
    }

}