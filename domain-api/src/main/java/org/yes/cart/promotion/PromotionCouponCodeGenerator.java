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

package org.yes.cart.promotion;

/**
 * User: denispavlov
 * Date: 06/06/2014
 * Time: 10:07
 */
public interface PromotionCouponCodeGenerator {

    /**
     * Generate random coupon code.
     *
     * @param shopCode shop code.
     *
     * @return generate a random coupon code (must check for uniqueness)
     */
    String generate(String shopCode);

}
