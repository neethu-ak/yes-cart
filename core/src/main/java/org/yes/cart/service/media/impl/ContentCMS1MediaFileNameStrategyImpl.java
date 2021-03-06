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

package org.yes.cart.service.media.impl;

import org.yes.cart.dao.GenericDAO;
import org.yes.cart.domain.entity.AttrValueCategory;
import org.yes.cart.service.misc.LanguageService;

import java.io.File;

/**
 * User: denispavlov
 * Date: 27/04/2019
 * Time: 15:15
 */
public class ContentCMS1MediaFileNameStrategyImpl extends CategoryMediaFileNameStrategyImpl {

    /**
     * Construct image name strategy
     *
     * @param urlPath                       URL path that identifies this strategy
     * @param relativeInternalRootDirectory internal image relative path root directory without {@link File#separator}. E.g. "category"
     * @param attrValueCategoryDao          category attributes dao
     * @param languageService               language service
     */
    public ContentCMS1MediaFileNameStrategyImpl(final String urlPath,
                                                final String relativeInternalRootDirectory,
                                                final GenericDAO<AttrValueCategory, Long> attrValueCategoryDao,
                                                final LanguageService languageService) {
        super(urlPath, relativeInternalRootDirectory, attrValueCategoryDao, languageService);
    }

}
