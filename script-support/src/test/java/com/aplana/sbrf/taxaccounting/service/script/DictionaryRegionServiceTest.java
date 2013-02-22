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
    private static DictionaryRegion regionTulsk = new DictionaryRegion(72, "Тюменская область", "71000000000", "71");
    private static DictionaryRegion regionHant = new DictionaryRegion(86, "Ханты-Мансийский автономный округ - Югра", "71100000000", "71100");

    @BeforeClass
    public static void tearUp() {
        DictionaryRegionDao dictionaryRegionDao = mock(DictionaryRegionDao.class);
        List<DictionaryRegion> values = new ArrayList<DictionaryRegion>();
        values.add(regionTulsk);
        values.add(regionHant);
        when(dictionaryRegionDao.getListRegions()).thenReturn(values);

        ReflectionTestUtils.setField(service, "dictionaryRegionDao", dictionaryRegionDao);
    }

    @Test
    public void testRegionByOkadoOrg() {
        assertNull("Должен не найти не одного региона", service.getRegionByOkadoOrg("88100001234"));
        assertEquals(regionHant, service.getRegionByOkadoOrg("71100001234"));
        assertEquals(regionTulsk, service.getRegionByOkadoOrg("71600001234"));
    }

    @Test
    public void test() {
        assertTrue(service.isValidCodeAndName(86, "Ханты-Мансийский автономный округ - Югра"));
        assertFalse(service.isValidCodeAndName(86, "Ханты-Мансийский автономный округ - Юграsadsadasdasdasda")); // неправильное имя
        assertFalse(service.isValidCodeAndName(87, "Ханты-Мансийский автономный округ - Югра")); // неправильный код
    }
}
