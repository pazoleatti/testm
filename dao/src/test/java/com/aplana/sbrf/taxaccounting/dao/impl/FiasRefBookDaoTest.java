package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.FiasRefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Table;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FiasRefBookDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FiasRefBookDaoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    RefBookDao refBookDao;

    @Test
    public void findAddressUtils() {
        Assert.assertEquals("#foo#bar#baz", FiasRefBookDaoImpl.createPath("foo", "bar", "baz"));
        Assert.assertEquals("#foo#bar", FiasRefBookDaoImpl.createPath("foo", null, "bar", null));
        Assert.assertEquals("#foo", FiasRefBookDaoImpl.createPath("foo"));
        Assert.assertEquals(null, FiasRefBookDaoImpl.createPath(""));
        Assert.assertEquals(null, FiasRefBookDaoImpl.createPath(null));
        Assert.assertEquals(null, FiasRefBookDaoImpl.createPath());
        Assert.assertEquals("baz", FiasRefBookDaoImpl.getLeaf("foo", "bar", "baz"));
        Assert.assertEquals("bar", FiasRefBookDaoImpl.getLeaf("foo", null, "bar", null));
        Assert.assertEquals("foo", FiasRefBookDaoImpl.getLeaf("foo"));
        Assert.assertEquals(null, FiasRefBookDaoImpl.getLeaf(""));
        Assert.assertEquals(null, FiasRefBookDaoImpl.getLeaf(null));
        Assert.assertEquals(null, FiasRefBookDaoImpl.getLeaf());
    }
}
