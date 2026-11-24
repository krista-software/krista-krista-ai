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
import app.krista.ksdk.kristaai.dto.AiModelVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static app.krista.extensions.krista.sdk.kristaai.util.AiResponseKeys.ERROR;
import static app.krista.extensions.krista.sdk.kristaai.util.AiResponseKeys.MESSAGE;
import static app.krista.extensions.krista.sdk.kristaai.util.ModelUtils.validateModelExistsAndAccessibleOrThrowError;

@Domain(id = "catEntryDomain_c2394eb0-02d9-4916-a3dc-909b1c631ea0",
        name = "SDK",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "f11e6c89-11b1-4264-ac9c-a7f174fe1e6c")
public class ModelsArea {

    private final ModelStore modelStore;
    private final AiModel aiModel;
    private final TelemetryHelper telemetryHelper;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelsArea.class);

    @Inject
    public ModelsArea(ModelStore modelStore, AiModel aiModel, TelemetryHelper telemetryHelper) {
        this.modelStore = modelStore;
        this.aiModel = aiModel;
        this.telemetryHelper = telemetryHelper;
    }

    /**
     * Retrieves all available versions of a specified AI model.
     * Returns version statistics including live status, accuracy, and creation date.
     *
     * @param modelId the unique identifier of the model to fetch versions for
     * @return a map containing version information with keys: Version Stats (list), Version Names (list),
     *         ERROR (boolean), MESSAGE (string)
     */
    @CatalogRequest(
            id = "localDomainRequest_124f82b6-fe75-4f3f-bce9-130f7e65b262",
            name = "Get Model Versions",
            description = "Fetches the list of all available versions of the model maintained by Krista-AI",
            area = "Models",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Version Stats", type = "[ { Live: Boolean, Version: Text, Accuracy: Number, Creation Date: Date } ]", required = false)
    @Field.Desc(name = "Version Names", type = "[ Text ]", required = false)
    @Field.Boolean(name = ERROR, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = MESSAGE, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Map<String, Object> listModelVersions(
            @Field(name = "Model Id", type = "Identifier", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String modelId) {

        TelemetryHelper.TelemetryOperation operation = telemetryHelper.startOperation("listModelVersions");

        List<String> versionNames = new LinkedList<>();
        List<Map<String, Object>> versionStats = new LinkedList<>();

        try {
            LOGGER.info("Fetching model versions for model ID: {}", modelId);
            validateModelExistsAndAccessibleOrThrowError(modelStore, modelId);

            // Get model Id using the model name.
            final List<AiModelVersionDetails> versionDetailsList = this.aiModel.getModelVersions(modelId);
            for (AiModelVersionDetails versionDetails : versionDetailsList) {
                versionNames.add(versionDetails.getVersion());
                versionStats.add(Map.of(
                        "Version", versionDetails.getVersion(),
                        "Live", versionDetails.isLive(),
                        "Accuracy", (double) versionDetails.getAccuracy(),
                        "Creation Date", versionDetails.getCreated()));
            }
            LOGGER.info("Successfully fetched {} versions for model ID: {}", versionDetailsList.size(), modelId);
            operation.recordSuccess(versionDetailsList.size());
            return Map.of("Version Stats", versionStats, "Version Names", versionNames, ERROR, false, MESSAGE, "");
        } catch (IllegalStateException cause) {
            LOGGER.error("Failed to fetch model versions for model ID '{}': {}", modelId, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of("Version Stats", versionStats, "Version Names", versionNames, ERROR, true, MESSAGE, cause.getMessage() == null ? "" : cause.getMessage());
        } catch (RuntimeException cause) {
            LOGGER.error("Unexpected error while fetching model versions for model ID '{}': {}", modelId, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of("Version Stats", versionStats, "Version Names", versionNames, ERROR, true, MESSAGE, "An unexpected error occurred while fetching model versions. " + (cause.getMessage() == null ? "" : cause.getMessage()));
        }
    }

    /**
     * Changes the live (active) version of a model to the specified target version.
     * Only the live version serves inference requests (predictions).
     *
     * @param modelId the unique identifier of the model
     * @param targetLiveVersion the version string to set as the new live version
     * @return a map containing operation status with keys: ERROR (boolean), MESSAGE (string)
     */
    @CatalogRequest(
            id = "localDomainRequest_a01b98e4-937d-441a-bbcf-2f491a57f7ba",
            name = "Change Live Version",
            description = "Any of the available version of a model can be taken to live (or active).\nInferences (predictions) are served only through the live version",
            area = "Models",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = ERROR, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    @Field.Text(name = MESSAGE, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Map<String, Object> changeLiveVersion(
            @Field(name = "Model Id", type = "Identifier", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String modelId,
            @Field.Text(name = "Target Live Version", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String targetLiveVersion) {

        TelemetryHelper.TelemetryOperation operation = telemetryHelper.startOperation("changeLiveVersion");

        try {
            LOGGER.info("Changing live version for model ID '{}' to version '{}'", modelId, targetLiveVersion);
            validateModelExistsAndAccessibleOrThrowError(modelStore, modelId);

            this.aiModel.activateModelVersion(modelId, targetLiveVersion);
            LOGGER.info("Successfully changed live version for model ID '{}' to version '{}'", modelId, targetLiveVersion);
            operation.recordSuccess();
            return Map.of(ERROR, false, MESSAGE, "Live version changed successfully!");
        } catch (IllegalStateException cause) {
            LOGGER.error("Failed to change live version for model ID '{}' to version '{}': {}", modelId, targetLiveVersion, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of(ERROR, true, MESSAGE, cause.getMessage() == null ? "" : cause.getMessage());
        } catch (RuntimeException cause) {
            LOGGER.error("Unexpected error while changing live version for model ID '{}' to version '{}': {}", modelId, targetLiveVersion, cause.getMessage(), cause);
            operation.recordFailure(cause);
            return Map.of(ERROR, true, MESSAGE, "An unexpected error occurred while changing the live version. " + (cause.getMessage() == null ? "" : cause.getMessage()));
        }
    }

}