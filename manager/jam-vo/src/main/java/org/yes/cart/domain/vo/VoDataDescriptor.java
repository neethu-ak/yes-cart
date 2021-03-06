/*
 * Copyright 2009 Inspire-Software.com
 *
 *    Licensed under the Apache License, Version 2.0 (the 'License');
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an 'AS IS' BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.yes.cart.domain.vo;

import com.inspiresoftware.lib.dto.geda.annotations.Dto;
import com.inspiresoftware.lib.dto.geda.annotations.DtoField;

/**
 * User: denispavlov
 * Date: 30/03/2019
 * Time: 12:12
 */
@Dto
public class VoDataDescriptor {

    @DtoField(value = "datadescriptorId", readOnly = true)
    private long datadescriptorId;

    @DtoField(value = "name")
    private String name;

    @DtoField(value = "type")
    private String type;

    @DtoField(value = "value")
    private String value;
    
    public long getDatadescriptorId() {
        return datadescriptorId;
    }

    public void setDatadescriptorId(final long datadescriptorId) {
        this.datadescriptorId = datadescriptorId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
