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

package com.onixbyte.icalendar.datatype;

import com.onixbyte.icalendar.property.Resolvable;

import java.net.URI;

public final class CalendarUserAddress implements Resolvable {

    private URI value;

    public CalendarUserAddress(URI value) {
        if (!"mailto".equalsIgnoreCase(value.getScheme())) {
            throw new IllegalArgumentException("Calendar User Address (CAL-ADDRESS) only accept mailto URI.");
        }
        this.value = value;
    }

    public CalendarUserAddress(String value) {
        var uri = URI.create(value);
        if (!"mailto".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Calendar User Address (CAL-ADDRESS) only accept mailto URI.");
        }
        this.value = uri;
    }

    @Override
    public String resolve() {
        return toString();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
