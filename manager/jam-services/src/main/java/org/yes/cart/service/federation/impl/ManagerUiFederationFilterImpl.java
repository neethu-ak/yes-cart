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

package org.yes.cart.service.federation.impl;

import org.yes.cart.domain.dto.ManagerDTO;
import org.yes.cart.service.dto.ManagementService;
import org.yes.cart.service.federation.FederationFilter;
import org.yes.cart.service.federation.ShopFederationStrategy;

import java.util.Collection;
import java.util.Iterator;

/**
 * User: denispavlov
 * Date: 16/09/2014
 * Time: 14:27
 */
public class ManagerUiFederationFilterImpl implements FederationFilter {

    private final ShopFederationStrategy shopFederationStrategy;
    private final ManagementService managementService;

    public ManagerUiFederationFilterImpl(final ShopFederationStrategy shopFederationStrategy,
                                         final ManagementService managementService) {
        this.shopFederationStrategy = shopFederationStrategy;
        this.managementService = managementService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyFederationFilter(final Collection list, final Class objectType) {
        final Iterator<ManagerDTO> managersIt = list.iterator();
        while (managersIt.hasNext()) {
            final ManagerDTO manager = managersIt.next();

            try {
                boolean manageable = shopFederationStrategy.isEmployeeManageableByCurrentManager(manager.getEmail());
                if (!manageable) {
                    managersIt.remove();
                }
            } catch (Exception exp) {
                managersIt.remove();
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isManageable(final Object object, final Class objectType) {
        try {
            return shopFederationStrategy.isEmployeeManageableByCurrentManager((String) object);
        } catch (Exception exp) {
            // nothing
        }
        return false;
    }

}
