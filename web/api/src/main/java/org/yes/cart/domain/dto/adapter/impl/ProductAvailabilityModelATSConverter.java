/*
 * Copyright 2009 Inspire-Software.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.domain.dto.adapter.impl;

import com.inspiresoftware.lib.dto.geda.adapter.BeanFactory;
import com.inspiresoftware.lib.dto.geda.adapter.ValueConverter;
import org.yes.cart.domain.entity.ProductAvailabilityModel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * User: denispavlov
 * Date: 23/03/2015
 * Time: 12:51
 */
public class ProductAvailabilityModelATSConverter implements ValueConverter {

    /** {@inheritDoc} */
    @Override
    public Object convertToDto(final Object object, final BeanFactory beanFactory) {

        final ProductAvailabilityModel model = (ProductAvailabilityModel) object;

        final Map<String, BigDecimal> values = new HashMap<>();

        for (final String sku : model.getSkuCodes()) {

            values.put(sku, model.getAvailableToSellQuantity(sku));

        }

        return values;
    }

    /** {@inheritDoc} */
    @Override
    public Object convertToEntity(final Object object, final Object oldEntity, final BeanFactory beanFactory) {
        throw new UnsupportedOperationException();
    }
}
