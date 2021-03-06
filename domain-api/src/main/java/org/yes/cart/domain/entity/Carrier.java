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

package org.yes.cart.domain.entity;

import org.yes.cart.domain.i18n.I18NModel;

import java.util.Collection;

/**
 * Present carrier / shipper entity.
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 07-May-2011
 * Time: 11:12:54
 */
public interface Carrier extends Auditable {

    /**
     * Get pk value.
     *
     * @return pk value.
     */
    long getCarrierId();

    /**
     * Set pk value.
     *
     * @param carrierId pk value.
     */
    void setCarrierId(long carrierId);

    /**
     * Get carrier name.
     *
     * @return carrier name
     */
    String getName();

    /**
     * Set carrier name.
     *
     * @param name name to set.
     */
    void setName(String name);

    /**
     * Get name.
     *
     * @return localisable name of carrier.
     */
    I18NModel getDisplayName();

    /**
     * Set name of carrier.
     *
     * @param name localisable name.
     */
    void setDisplayName(I18NModel name);

    /**
     * Get carrier description.
     *
     * @return description
     */
    String getDescription();

    /**
     * Set carrier description.
     *
     * @param description description
     */
    void setDescription(String description);

    /**
     * Get name.
     *
     * @return localisable description of carrier.
     */
    I18NModel getDisplayDescription();

    /**
     * Set description of carrier.
     *
     * @param description localisable description.
     */
    void setDisplayDescription(I18NModel description);

    /**
     * Get the list of carrier SLAs.
     *
     * @return carrier SLAs
     */
    Collection<CarrierSla> getCarrierSla();

    /**
     * Set carrier SLAs
     *
     * @param carrierSla SLA to set.
     */
    void setCarrierSla(Collection<CarrierSla> carrierSla);


    /**
     * Can carrier perform world wide delivery.
     *
     * @return true if performs delivery.
     */
    boolean isWorldwide();

    /**
     * Set world wide delivery flag.
     *
     * @param worldwide world wide delivery flag
     */
    void setWorldwide(boolean worldwide);

    /**
     * Can carrier perform country delivery.
     *
     * @return true if performs country delivery.
     */
    boolean isCountry();

    /**
     * Set country delivery flag.
     *
     * @param country country delivery flag.
     */
    void setCountry(boolean country);

    /**
     * Can carrier perform state delivery.
     *
     * @return true if performs state delivery.
     */

    boolean isState();

    /**
     * Set state delivery flag.
     *
     * @param state state delivery flag
     */
    void setState(boolean state);

    /**
     * Can carrier perform local (city) delivery.
     *
     * @return true if performs local (city)  delivery.
     */

    boolean isLocal();

    /**
     * Set local delivery flag.
     *
     * @param local local delivery flag.
     */
    void setLocal(boolean local);


    /**
     * Get assigned shops.
     *
     * @return shops
     */
    Collection<CarrierShop> getShops();

    /**
     * Set assigned shops.
     *
     * @param shops shops
     */
    void setShops(Collection<CarrierShop> shops);



}


