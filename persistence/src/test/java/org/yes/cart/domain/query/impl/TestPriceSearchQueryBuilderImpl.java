package org.yes.cart.domain.query.impl;

import org.apache.lucene.search.BooleanQuery;
import org.junit.Before;
import org.junit.Test;
import org.yes.cart.dao.GenericDAO;
import org.yes.cart.dao.constants.DaoServiceBeanKeys;
import org.yes.cart.dao.impl.AbstractTestDAO;
import org.yes.cart.domain.entity.Product;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 08-May-2011
 * Time: 11:12:54
 */
public class TestPriceSearchQueryBuilderImpl extends AbstractTestDAO {

    private GenericDAO<Product, Long> productDao;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        productDao = (GenericDAO<Product, Long>) ctx.getBean(DaoServiceBeanKeys.PRODUCT_DAO);
    }

    public void cleanUp() {

    }

    public void testDummy() {

    }

    @Test
    public void testPriceSearchQueryBuilder() {

        productDao.fullTextSearchReindex();

        PriceSearchQueryBuilderImpl queryBuilder = new PriceSearchQueryBuilderImpl();

        // test that price border is inclusive in search
        BooleanQuery query = queryBuilder.createQuery(
                Arrays.asList(129L, 130L, 131L, 132L),
                10L, "EUR", new BigDecimal(12), new BigDecimal(15)
        );
        List<Product> products = productDao.fullTextSearch(query);
        assertNotNull(query.toString(), products);
        assertFalse(query.toString(), products.isEmpty());
        assertEquals(1, products.size());

        // Test that filter by categories works. Categories 131 and 132
        // not contains product with price 15.00
        query = queryBuilder.createQuery(
                Arrays.asList(131L, 132L),
                10L, "EUR", new BigDecimal(15), new BigDecimal(15)
        );
        products = productDao.fullTextSearch(query);
        assertNotNull(query.toString(), products);
        assertTrue(query.toString(), products.isEmpty());

        // no prices less than 15
        query = queryBuilder.createQuery(
                Arrays.asList(129L, 130L, 131L, 132L),
                10L, "EUR", new BigDecimal(0), new BigDecimal(14)
        );
        products = productDao.fullTextSearch(query);
        assertNotNull(query.toString(), products);
        assertTrue(query.toString(), products.isEmpty());

        // Test, that we able to getByKey all products,
        // that match search criteria
        query = queryBuilder.createQuery(
                Arrays.asList(129L, 130L, 131L, 132L),
                10L, "EUR", new BigDecimal(0), new BigDecimal(1000)
        );
        products = productDao.fullTextSearch(query);
        assertNotNull(query.toString(), products);
        assertTrue(query.toString(), !products.isEmpty());
        assertEquals(5, products.size()); //Here 2 product have 0 quantity on warehouse

        // no price 250 in given categories
        query = queryBuilder.createQuery(
                Arrays.asList(129L, 130L, 132L),
                10L, "EUR", new BigDecimal(250), new BigDecimal(420)
        );
        products = productDao.fullTextSearch(query);
        assertNotNull(query.toString(), products);
        assertTrue(query.toString(), !products.isEmpty());
        assertEquals(2, products.size());


        // Test, that filter by currency code works ok
        query = queryBuilder.createQuery(
                Arrays.asList(129L, 130L, 131L, 132L),
                10L, "USD", new BigDecimal(0), new BigDecimal(1000)
        );
        products = productDao.fullTextSearch(query);
        assertNotNull(query.toString(), products);
        assertTrue(query.toString(), products.isEmpty());

        // Test, that filter by shop id works
        query = queryBuilder.createQuery(
                Arrays.asList(129L, 130L, 131L, 132L),
                20L, "EUR", new BigDecimal(0), new BigDecimal(1000)
        );
        products = productDao.fullTextSearch(query);
        assertNotNull(query.toString(), products);
        assertTrue(query.toString(), products.isEmpty());

    }

}
