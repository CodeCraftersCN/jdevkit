/*
 * Copyright (C) 2024-2024 OnixByte.
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onixbyte.icalendar.component.property;

import com.onixbyte.icalendar.property.parameter.AlternateRepresentation;
import com.onixbyte.icalendar.property.parameter.Language;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public interface TextProperty {

    default String composeResolution(String propertyName,
                                     AlternateRepresentation altRep,
                                     Language language,
                                     String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return null;
        }

        var resolutionBuilder = new StringBuilder(propertyName);

        Optional.ofNullable(altRep)
                .ifPresent((_altRep) -> resolutionBuilder.append(";").append(_altRep.resolve()));

        Optional.ofNullable(language)
                .ifPresent((lang) -> resolutionBuilder.append(";").append(lang.resolve()));

        resolutionBuilder.append(":")
                .append(value)
                .append("\n");

        return resolutionBuilder.toString();
    }

    default String composeResolution(String propertyName,
                                     AlternateRepresentation altRep,
                                     Language language,
                                     Supplier<String> valueSupplier) {
        return composeResolution(propertyName, altRep, language, valueSupplier.get());
    }

}
