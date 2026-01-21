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

import app.krista.extension.impl.anno.*;
import app.krista.ksdk.kristaai.dto.AiModelDetails;

@Domain(id = "catEntryDomain_c2394eb0-02d9-4916-a3dc-909b1c631ea0",
        name = "SDK",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion ="d5d9d1ec-b4c0-43e8-90b2-48137a0ac4fa")
@Entity(name = "Model", id = "localDomainEntity_d00e4996-1972-490a-8215-10e620099967", primaryKey = "Model Id", supportStore = true, options = {})
public class Model {

    @ToString
    @Field(name = "Model Id", type = "Identifier", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String modelId;

    @Searchable
    @ToString
    @Field.Text(name = "Model Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String modelName;

    @ToString
    @Field.Date(name = "Creation Time", required = false, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = true, allowToday = true, allowFuture = false, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Long creationTime;

    /**
     * Creates a Model entity from an AiModelDetails DTO.
     *
     * @param aiModelDetail the AI model details containing model information
     * @return a new Model entity populated with data from the provided details
     */
    public static Model from(AiModelDetails aiModelDetail) {
        Model model = new Model();
        model.modelId = aiModelDetail.getModelId();
        model.modelName = aiModelDetail.getModelName();
        model.creationTime = aiModelDetail.getModelCreationTime();
        return model;
    }
}