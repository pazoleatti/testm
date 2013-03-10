package com.aplana.sbrf.taxaccounting.dao.script.dictionary;

import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import org.junit.BeforeClass;
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

    private static DictionaryRegion regionTulsk;
    private static DictionaryRegion regionHant;

	@BeforeClass
	public static void init() {
		regionTulsk = new DictionaryRegion();
		regionTulsk.setCode(72);
		regionTulsk.setName("Тюменская область");
		regionTulsk.setOkato("71000000000");
		regionTulsk.setOkatoDefinition("71");

		regionHant = new DictionaryRegion();
		regionHant.setCode(86);
		regionHant.setName("Ханты-Мансийский автономный округ - Югра");
		regionHant.setOkato("71000000000");
		regionHant.setOkatoDefinition("71100");
	}

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
