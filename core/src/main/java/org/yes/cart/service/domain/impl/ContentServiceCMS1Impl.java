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

package org.yes.cart.service.domain.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yes.cart.config.Configuration;
import org.yes.cart.config.ConfigurationContext;
import org.yes.cart.config.RegistrationAware;
import org.yes.cart.dao.GenericDAO;
import org.yes.cart.dao.ResultsIteratorCallback;
import org.yes.cart.domain.entity.AttrValue;
import org.yes.cart.domain.entity.Category;
import org.yes.cart.domain.entity.Content;
import org.yes.cart.domain.entity.impl.ContentCategoryAdapter;
import org.yes.cart.domain.i18n.impl.FailoverStringI18NModel;
import org.yes.cart.domain.misc.Pair;
import org.yes.cart.search.dao.support.ShopCategoryRelationshipSupport;
import org.yes.cart.service.domain.ContentService;
import org.yes.cart.service.theme.templates.TemplateProcessor;
import org.yes.cart.utils.HQLUtils;
import org.yes.cart.utils.TimeContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * User: Denis Pavlov
 * Date: 15-June-2013
 */
public class ContentServiceCMS1Impl implements ContentService, Configuration, RegistrationAware {

    private static final Logger LOG = LoggerFactory.getLogger(ContentServiceCMS1Impl.class);

    private final GenericDAO<Category, Long> categoryDao;

    private final ShopCategoryRelationshipSupport shopCategoryRelationshipSupport;

    private final BaseGenericServiceImpl<Category> categoryService;

    private final TemplateProcessor templateSupport;

    private ConfigurationContext cfgContext;

    /**
     * Construct service to manage content
     *
     * @param categoryDao                     category dao to use
     * @param shopCategoryRelationshipSupport category relationship support
     * @param templateSupport                 template support
     */
    public ContentServiceCMS1Impl(final GenericDAO<Category, Long> categoryDao,
                                  final ShopCategoryRelationshipSupport shopCategoryRelationshipSupport,
                                  final TemplateProcessor templateSupport) {
        this.categoryDao = categoryDao;
        this.shopCategoryRelationshipSupport = shopCategoryRelationshipSupport;
        this.categoryService = new BaseGenericServiceImpl<>(categoryDao);
        this.templateSupport = templateSupport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Content getRootContent(final long shopId) {
        if (shopId <= 0) {
            throw new IllegalArgumentException("Shop must not be null or transient");
        }
        final Category category = categoryDao.findSingleByNamedQuery("CMS1.ROOTCONTENT.BY.SHOP.ID", shopId);
        if (category != null) {
            return new ContentCategoryAdapter(category);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Content createRootContent(final long shopId) {
        final List<Object> shops = categoryDao.findQueryObjectByNamedQuery("SHOPCODE.BY.SHOP.ID", shopId);
        if (shops != null && shops.size() == 1) {
            return createContentRootForShop((String) shops.get(0));
        }
        throw new IllegalArgumentException("Unidentified shop id");
    }

    private Content createContentRootForShop(final String shopcode) {
        final LocalDateTime now = now();
        final Category root = categoryDao.getEntityFactory().getByIface(Category.class);
        root.setGuid(shopcode);
        root.setName(shopcode);
        root.setParentId(0L);
        root.setUitemplate("content");
        root.setDisabled(false);
        root.setAvailablefrom(now.truncatedTo(ChronoUnit.DAYS));
        root.setAvailableto(now.plusYears(100).truncatedTo(ChronoUnit.DAYS));
        root.setNavigationByPrice(false);
        root.setNavigationByPriceTiers("");
        root.setNavigationByAttributes(false);
        categoryDao.saveOrUpdate(root);
        return new ContentCategoryAdapter(root);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Long> getShopContentIds(final long shopId) {

        final Set<Long> result = new HashSet<>();

        final Map<Long, Set<Long>> map = shopCategoryRelationshipSupport.getAllCategoriesIdsMap();

        final Category category = categoryDao.findSingleByNamedQuery("CMS1.ROOTCONTENT.BY.SHOP.ID", shopId);

        if (category != null) {
            appendChildren(result, category.getCategoryId(), map);
        }

        return Collections.unmodifiableSet(result);

    }

    private void appendChildren(final Set<Long> result, final long currentId, final Map<Long, Set<Long>> map) {

        result.add(currentId);

        final Set<Long> immediateChildren = map.get(currentId);
        if (CollectionUtils.isNotEmpty(immediateChildren)) {
            for (final Long child : immediateChildren) {
                appendChildren(result, child, map);
            }
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentTemplate(final long contentId) {
        final Content content = proxy().findById(contentId);
        if (content != null && !content.isRoot()) {
            if (StringUtils.isBlank(content.getUitemplate())) {
                return proxy().getContentTemplate(content.getParentId());
            } else {
                return content.getUitemplate();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentBody(final long contentId, final String locale) {
        final String attributeKey = "CONTENT_BODY_" + locale + "_%";
        final List<Object> bodyList = categoryDao.findQueryObjectByNamedQuery("CMS1.CONTENTBODY.BY.CONTENTID", contentId, attributeKey, now(), Boolean.FALSE);
        if (bodyList != null && bodyList.size() > 0) {
            final StringBuilder content = new StringBuilder();
            for (final Object bodyPart : bodyList) {
                if (StringUtils.isNotBlank((String) bodyPart)) {
                    content.append(bodyPart);
                }
            }
            return content.toString();
        }
        return "";
    }

    LocalDateTime now() {
        return TimeContext.getLocalDateTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentBody(final String contentUri, final String locale) {
        final Long id = findContentIdBySeoUri(contentUri);
        if (id != null) {
            return proxy().getContentBody(id, locale);
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDynamicContentBody(final long contentId, final String locale, final Map<String, Object> context) {

        final String rawContent = proxy().getContentBody(contentId, locale);

        if (StringUtils.isNotBlank(rawContent)) {

            return this.templateSupport.processTemplate(rawContent, locale, context);

        }

        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDynamicContentBody(final String contentUri, final String locale, final Map<String, Object> context) {

        final String rawContent = proxy().getContentBody(contentUri, locale);

        if (StringUtils.isNotBlank(rawContent)) {

            return this.templateSupport.processTemplate(rawContent, locale, context);

        }

        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentAttributeRecursive(final String locale, final long contentId, final String attributeName, final String defaultValue) {

        final Content content = proxy().getById(contentId);

        if (content == null || attributeName == null || content.isRoot()) {
            return defaultValue;
        }

        final AttrValue attrValue = content.getAttributeByCode(attributeName);
        if (attrValue != null) {
            final String val;
            if (locale == null) {
                val = attrValue.getVal();
            } else {
                val = new FailoverStringI18NModel(attrValue.getDisplayVal(), attrValue.getVal()).getValue(locale);
            }
            if (!StringUtils.isBlank(val)) {
                return val;
            }
        }

        return proxy().getContentAttributeRecursive(locale, content.getParentId(), attributeName, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getContentAttributeRecursive(final String locale, final long contentId, final String[] attributeNames) {

        final Content content;

        if (contentId > 0L && attributeNames != null && attributeNames.length > 0) {
            content = proxy().getById(contentId);
        } else {
            return null;
        }

        if (content == null) {
            return null;
        }

        final String[] rez = new String[attributeNames.length];
        boolean hasValue = false;
        for (int i = 0; i < attributeNames.length; i++) {
            final String attributeName = attributeNames[i];
            final String val = proxy().getContentAttributeRecursive(locale, contentId, attributeName, null);
            if (val != null) {
                hasValue = true;
            }
            rez[i] = val;
        }

        if (hasValue) {
            return rez;
        }
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Content> getChildContent(final long contentId) {
        return findChildContentWithAvailability(contentId, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Content> findChildContentWithAvailability(final long contentId, final boolean withAvailability) {

        final List<Category> cats = new ArrayList<>(categoryDao.findByNamedQuery(
                "CMS1.CONTENT.BY.PARENTID.WITHOUT.DATE.FILTERING",
                contentId
        ));
        if (withAvailability) {

            final LocalDateTime now = now();
            cats.removeIf(cat -> !cat.isAvailable(now));

        }
        final List<Content> cnt = new ArrayList<>();
        for (final Category cat : cats) {
            cnt.add(new ContentCategoryAdapter(cat));
        }
        return cnt;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Content> getChildContentRecursive(final long contentId) {
        final Content thisCon = proxy().getById(contentId);
        if (thisCon != null) {
            final Set<Content> all = new HashSet<>();
            all.add(thisCon);
            loadChildContentRecursiveInternal(all, thisCon);
            return all;
        }
        return Collections.emptySet();
    }


    private void loadChildContentRecursiveInternal(final Set<Content> result, final Content category) {
        List<Content> categories = proxy().getChildContent(category.getContentId());
        result.addAll(categories);
        for (Content subCategory : categories) {
            loadChildContentRecursiveInternal(result, subCategory);
        }
    }



    private Pair<String, Object[]> findContentQuery(final boolean count,
                                                    final String sort,
                                                    final boolean sortDescending,
                                                    final Map<String, List> filter) {

        final Map<String, List> currentFilter = filter != null ? new HashMap<>(filter) : null;

        final StringBuilder hqlCriteria = new StringBuilder();
        final List<Object> params = new ArrayList<>();

        if (count) {
            hqlCriteria.append("select count(c.categoryId) from CategoryEntity c ");
        } else {
            hqlCriteria.append("select c from CategoryEntity c ");
        }

        final List categoryIds = currentFilter != null ? currentFilter.remove("contentIds") : null;
        if (categoryIds != null) {
            hqlCriteria.append(" where (c.categoryId in (?1)) ");
            params.add(categoryIds);
        }

        HQLUtils.appendFilterCriteria(hqlCriteria, params, "c", currentFilter);


        if (StringUtils.isNotBlank(sort)) {

            hqlCriteria.append(" order by c." + sort + " " + (sortDescending ? "desc" : "asc"));

        }

        return new Pair<>(
                hqlCriteria.toString(),
                params.toArray(new Object[params.size()])
        );

    }




    /**
     * {@inheritDoc}
     */
    @Override
    public List<Content> findContent(final int start, final int offset, final String sort, final boolean sortDescending, final Map<String, List> filter) {

        final Pair<String, Object[]> query = findContentQuery(false, sort, sortDescending, filter);

        final List<Category> cats = (List) getGenericDao().findRangeByQuery(
                query.getFirst(),
                start, offset,
                query.getSecond()
        );

        final List<Content> cnt = new ArrayList<>();
        for (final Category cat : cats) {
            cnt.add(new ContentCategoryAdapter(cat));
        }
        return cnt;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int findContentCount(final Map<String, List> filter) {

        final Pair<String, Object[]> query = findContentQuery(true, null, false, filter);

        return getGenericDao().findCountByQuery(
                query.getFirst(),
                query.getSecond()
        );
    }


    /**
     * {@inheritDoc} Just to cache
     */
    @Override
    public Content getById(final long pk) {
        final Category cat = categoryDao.findById(pk);
        if (cat != null) {
            return new ContentCategoryAdapter(cat);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long findContentIdBySeoUri(final String seoUri) {
        List<Object> list = categoryDao.findQueryObjectByNamedQuery("CMS1.CONTENT.ID.BY.SEO.URI", seoUri);
        if (list != null && !list.isEmpty()) {
            final Object id = list.get(0);
            if (id instanceof Long) {
                return (Long) id;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long findContentIdByGUID(final String guid) {
        List<Object> list = categoryDao.findQueryObjectByNamedQuery("CMS1.CONTENT.ID.BY.GUID", guid);
        if (list != null && !list.isEmpty()) {
            final Object id = list.get(0);
            if (id instanceof Long) {
                return (Long) id;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String findSeoUriByContentId(final Long contentId) {
        List<Object> list = categoryDao.findQueryObjectByNamedQuery("CMS1.SEO.URI.BY.CONTENT.ID", contentId);
        if (list != null && !list.isEmpty()) {
            final Object[] uriAndId = (Object[]) list.get(0);
            if (uriAndId[0] instanceof String) {
                return (String) uriAndId[0];
            }
            return String.valueOf(uriAndId[1]);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Content findContentIdBySeoUriOrGuid(final String seoUriOrGuid) {

        Category content = categoryDao.findSingleByNamedQuery("CMS1.CONTENT.BY.SEO.URI", seoUriOrGuid);
        if (content == null) {
            content = categoryDao.findSingleByNamedQuery("CMS1.CONTENT.BY.GUID", seoUriOrGuid);
        }
        if (content == null) {
            return null;
        }
        return new ContentCategoryAdapter(content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContentHasSubcontent(final long topContentId, final long subContentId) {
        final Content start = proxy().getById(subContentId);
        if (start != null) {
            if (subContentId == topContentId) {
                return true;
            } else {
                final List<Content> list = new ArrayList<>();
                list.add(start);
                addParent(list, topContentId);
                return list.get(list.size() - 1).getContentId() == topContentId;
            }
        }
        return false;
    }

    private void addParent(final List<Content> contentChain, final long contentIdStopAt) {
        final Content cat = contentChain.get(contentChain.size() - 1);
        if (!cat.isRoot()) {
            final Content parent = proxy().getById(cat.getParentId());
            if (parent != null) {
                contentChain.add(parent);
                if (parent.getContentId() != contentIdStopAt) {
                    addParent(contentChain, contentIdStopAt);
                }
            }
        }
    }

    private ContentService proxy;

    private ContentService proxy() {
        if (proxy == null) {
            proxy = getSelf();
        }
        return proxy;
    }

    /**
     * @return self proxy to reuse AOP caching
     */
    public ContentService getSelf() {
        // Spring lookup method to get self proxy
        return null;
    }


    @Override
    public List<Content> findAll() {

        final List<Category> all = this.categoryService.findAll();
        final List<Content> cnt = new ArrayList<>();
        for (final Category cat : all) {
            cnt.add(new ContentCategoryAdapter(cat));
        }
        return cnt;

    }

    @Override
    public void findAllIterator(final ResultsIteratorCallback<Content> callback) {

        this.categoryService.findAllIterator(entity -> callback.withNext(new ContentCategoryAdapter(entity)));

    }

    @Override
    public void findByCriteriaIterator(final String eCriteria, final Object[] parameters, final ResultsIteratorCallback<Content> callback) {

        this.categoryService.findByCriteriaIterator(eCriteria, parameters, entity -> callback.withNext(new ContentCategoryAdapter(entity)));

    }

    @Override
    public Content findById(final long pk) {

        final Category cat = this.categoryService.findById(pk);
        if (cat != null) {
            return new ContentCategoryAdapter(cat);
        }
        return null;

    }

    @Override
    public Content create(final Content instance) {

        if (instance instanceof ContentCategoryAdapter) {

            return new ContentCategoryAdapter(this.categoryService.create(((ContentCategoryAdapter) instance).getCategory()));

        }

        throw new UnsupportedOperationException("CMS1 accepts only ContentCategoryAdapter objects");

    }

    @Override
    public Content update(final Content instance) {

        if (instance instanceof ContentCategoryAdapter) {

            return new ContentCategoryAdapter(this.categoryService.update(((ContentCategoryAdapter) instance).getCategory()));

        }

        throw new UnsupportedOperationException("CMS1 accepts only ContentCategoryAdapter objects");

    }

    @Override
    public void delete(final Content instance) {

        if (instance instanceof ContentCategoryAdapter) {

            this.categoryService.delete(((ContentCategoryAdapter) instance).getCategory());

        } else {

            throw new UnsupportedOperationException("CMS1 accepts only ContentCategoryAdapter objects");

        }

    }

    @Override
    public List<Content> findByCriteria(final String eCriteria, final Object... parameters) {

        final List<Category> all = this.categoryService.findByCriteria(eCriteria, parameters);
        final List<Content> cnt = new ArrayList<>();
        for (final Category cat : all) {
            cnt.add(new ContentCategoryAdapter(cat));
        }
        return cnt;

    }

    @Override
    public int findCountByCriteria(final String eCriteria, final Object... parameters) {

        return this.categoryService.findCountByCriteria(eCriteria, parameters);

    }

    @Override
    public Content findSingleByCriteria(final String eCriteria, final Object... parameters) {

        final Category cat = this.categoryService.findSingleByCriteria(eCriteria, parameters);
        if (cat != null) {
            return new ContentCategoryAdapter(cat);
        }
        return null;

    }

    @Override
    public GenericDAO<Content, Long> getGenericDao() {
        return (GenericDAO) this.categoryDao; // Hack, other layers should not be using this
    }


    /** {@inheritDoc} */
    @Override
    public ConfigurationContext getCfgContext() {
        return cfgContext;
    }

    public void setCfgContext(final ConfigurationContext cfgContext) {
        this.cfgContext = cfgContext;
    }

    /** {@inheritDoc} */
    @Override
    public void onRegisterEvent() {

        LOG.info("Defining CMS.include function for CMS1");

        this.templateSupport.registerFunction("include", params -> {

            if (params != null && params.length == 3) {

                final String uri = String.valueOf(params[0]);

                final Long contentId = proxy().findContentIdBySeoUri(uri);

                if (contentId != null) {
                    final String locale = String.valueOf(params[1]);
                    final Map<String, Object> context = (Map<String, Object>) params[2];


                    return proxy().getDynamicContentBody(contentId, locale, context);

                }

            }

            return "";
        });

    }
}
