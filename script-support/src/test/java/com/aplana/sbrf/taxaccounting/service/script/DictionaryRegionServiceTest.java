package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.DictionaryRegionDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryRegionService;
import com.aplana.sbrf.taxaccounting.service.script.dictionary.impl.DictionaryRegionServiceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: ekuvshinov
 * Date: 21.02.13
 */
public class DictionaryRegionServiceTest {
    private static DictionaryRegionService service = new DictionaryRegionServiceImpl();

    private static DictionaryRegion regionTulsk;
    private static DictionaryRegion regionHant;
    private static DictionaryRegion regionDefault;

    @BeforeClass
    public static void tearUp() {
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

		regionDefault = new DictionaryRegion();
		regionDefault.setCode(99);
		regionDefault.setName("Иные территории, включая город и космодром Байконур");

        DictionaryRegionDao dictionaryRegionDao = mock(DictionaryRegionDao.class);
        List<DictionaryRegion> values = new ArrayList<DictionaryRegion>();
        values.add(regionTulsk);
        values.add(regionHant);
        values.add(regionDefault);
        when(dictionaryRegionDao.getListRegions()).thenReturn(values);

        ReflectionTestUtils.setField(service, "dictionaryRegionDao", dictionaryRegionDao);
    }

    @Test
    public void testRegionByOkadoOrg() {
        assertNull("Должен не найти не одного региона", service.getRegionByOkatoOrg("88100001234"));
        assertEquals(regionHant, service.getRegionByOkatoOrg("71100001234"));
        assertEquals(regionTulsk, service.getRegionByOkatoOrg("71600001234"));
        assertEquals(regionDefault.getCode(), service.getRegionByOkatoOrg(null).getCode());
    }

    @Test
    public void testIsValidCode() {
        assertFalse(service.isValidCode(98313));
        assertTrue(service.isValidCode(72));
    }

    @Test
    public void testGetRegionByName () {
        assertNull(service.getRegionByName(null));
        assertEquals(regionHant, service.getRegionByName(regionHant.getName()));
    }

    @Test
    public void testGetRegionById () {
        assertEquals(regionHant, service.getRegionById(regionHant.getCode()));
        Boolean exception = false;
        try {
            service.getRegionById(21321312); // code must be invalid
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }
}
