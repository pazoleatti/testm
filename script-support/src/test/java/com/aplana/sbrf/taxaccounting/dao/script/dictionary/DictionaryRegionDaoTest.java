package com.aplana.sbrf.taxaccounting.dao.script.dictionary;

import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: ekuvshinov
 * Date: 21.02.13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DictionaryRegionDaoTest.xml"})
public class DictionaryRegionDaoTest {
    @Autowired
    private DictionaryRegionDao dao;
    private static DictionaryRegion regionTulsk = new DictionaryRegion(72, "Тюменская область", "71000000000", "71");
    private static DictionaryRegion regionHant = new DictionaryRegion(86, "Ханты-Мансийский автономный округ - Югра", "71000000000", "71100");

    @Test
    public void testSimple(){
        assertNotNull(dao);
    }
    @Test
    public void testGetListRegions() {
        List<DictionaryRegion> values = new ArrayList<DictionaryRegion>();
        values.add(regionTulsk);
        values.add(regionHant);
        assertArrayEquals(values.toArray(), dao.getListRegions().toArray());
    }
}
