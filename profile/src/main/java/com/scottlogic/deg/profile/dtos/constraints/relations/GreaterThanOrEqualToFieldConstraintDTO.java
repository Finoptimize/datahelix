/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.profile.dtos.constraints.relations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintType;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintTypeJsonProperty;

@JsonDeserialize(as = GreaterThanOrEqualToFieldConstraintDTO.class)
public class GreaterThanOrEqualToFieldConstraintDTO extends RelationalConstraintDTO {
    @JsonProperty(ConstraintTypeJsonProperty.GREATER_THAN_OR_EQUAL_TO_FIELD)
    public String otherField;

    public GreaterThanOrEqualToFieldConstraintDTO() {
        super(ConstraintType.GREATER_THAN_OR_EQUAL_TO_FIELD);
    }

    @Override
    public String getOtherField() {
        return otherField;
    }
}
