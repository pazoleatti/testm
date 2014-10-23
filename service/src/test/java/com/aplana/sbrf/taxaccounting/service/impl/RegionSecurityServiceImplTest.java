package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegionSecurityServiceImplTest {

    private RegionSecurityServiceImpl regionSecurityService = new RegionSecurityServiceImpl();
    private final String REGION_ALIAS = "someAlias";

    private final Long NOT_REGION_REF_BOOK_ID = 1L;
    private final Long REGION_REF_BOOK_ID = 2L;

    private final Long USER_REGION = 182L;

    private final Long UNIQUE_RECORD_ID = 1L;
    private final Long COMMON_RECORD_ID = 1L;
    private final Long PREV_RECORD_ID = 2L;

    private final Long DEPARTAMENT_ID = 1L;
    private final Long BAD_DEPARTAMENT_ID = 2L;

    private final Date START = new Date();

    @Before
    public void init() {
        // нерегиональный справочник
        RefBook notRegionRefBook = new RefBook();
        notRegionRefBook.setId(NOT_REGION_REF_BOOK_ID);
        notRegionRefBook.setRegionAttribute(null);

        // региональный справочник
        RefBook refBook = new RefBook();
        refBook.setId(REGION_REF_BOOK_ID);
        RefBookAttribute regionAttr = new RefBookAttribute();
        regionAttr.setAlias(REGION_ALIAS);
        refBook.setRegionAttribute(regionAttr);

        // подразделение пользователя
        RefBookDataProvider departmentProvider = mock(RefBookDataProvider.class);

        // подразделение пользователя с заданным регионом
        Map<String, RefBookValue> department = new HashMap<String, RefBookValue>();
        RefBookValue userRegion = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION);
        department.put("REGION_ID", userRegion);

        // подразделение пользователя без заданния региона
        Map<String, RefBookValue> badDepartment = new HashMap<String, RefBookValue>();
        badDepartment.put("REGION_ID", null);

        // справочник
        RefBookDataProvider provider = mock(RefBookDataProvider.class);

        // правильные старые значения
        Map<String, RefBookValue> record = new HashMap<String, RefBookValue>();
        RefBookValue region = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION);
        record.put(REGION_ALIAS, region);

        // версии записи
        List<Pair<Long, Long>> pairs = new ArrayList<Pair<Long, Long>>();
        Pair<Long, Long> pair = new Pair<Long, Long>(PREV_RECORD_ID, COMMON_RECORD_ID);
        pairs.add(pair);

        List<Long> list = new ArrayList<Long>();
        list.add(PREV_RECORD_ID);
        Map<Long, Date> versionDateMap = new HashMap<Long, Date>();
        versionDateMap.put(PREV_RECORD_ID, START);

        RefBookFactory refBookFactory = mock(RefBookFactory.class);
        ReflectionTestUtils.setField(regionSecurityService, "refBookFactory", refBookFactory);

        when(refBookFactory.get(NOT_REGION_REF_BOOK_ID)).thenReturn(notRegionRefBook);
        when(refBookFactory.get(REGION_REF_BOOK_ID)).thenReturn(refBook);

        when(refBookFactory.getDataProvider(30L)).thenReturn(departmentProvider);
        when(departmentProvider.getRecordData(DEPARTAMENT_ID)).thenReturn(department);
        when(departmentProvider.getRecordData(BAD_DEPARTAMENT_ID)).thenReturn(badDepartment);

        when(refBookFactory.getDataProvider(NOT_REGION_REF_BOOK_ID)).thenReturn(provider);
        when(refBookFactory.getDataProvider(REGION_REF_BOOK_ID)).thenReturn(provider);
        when(provider.getRecordData(UNIQUE_RECORD_ID)).thenReturn(record);
        when(provider.getRecordData(PREV_RECORD_ID)).thenReturn(record);

        when(provider.getRecordIdPairs(REGION_REF_BOOK_ID, START, false, null)).thenReturn(pairs);
        when(provider.getRecordsVersionStart(list)).thenReturn(versionDateMap);
    }

    @Test
    public void deleteRecordSuccessForUnp() {
        // для роли UNP всегда можно изменять
        TAUser user = getUser(TARole.ROLE_CONTROL_UNP);
        Long regionRefBook = REGION_REF_BOOK_ID;
        Long uniqueRecord = UNIQUE_RECORD_ID;
        boolean result = regionSecurityService.check(user, regionRefBook, uniqueRecord);
        Assert.assertTrue(result);

        Long notRegionRefBook = NOT_REGION_REF_BOOK_ID;
        result = regionSecurityService.check(user, notRegionRefBook, uniqueRecord);
        Assert.assertTrue(result);
    }

    @Test
    public void deleteRecordFailForNotUnpOrNs() {
        // для ролей не UNP и на NS нельзя изменять ни региональные и ни нерегиональные спавочники
        TAUser user = getUser(TARole.ROLE_CONTROL);
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        boolean result = regionSecurityService.check(user, regionRefBookId, uniqueRecordId);
        Assert.assertFalse(result);

        Long notRegionRefBookId = NOT_REGION_REF_BOOK_ID;
        result = regionSecurityService.check(user, notRegionRefBookId, uniqueRecordId);
        Assert.assertFalse(result);
    }

    @Test
    public void deleteRecordForNs() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        TAUser user = getUser(TARole.ROLE_CONTROL_NS);
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        boolean result = regionSecurityService.check(user, regionRefBookId, uniqueRecordId);
        Assert.assertTrue(result);

        regionRefBookId = NOT_REGION_REF_BOOK_ID;
        result = regionSecurityService.check(user, regionRefBookId, uniqueRecordId);
        Assert.assertFalse(result);
    }

    @Test
    public void deleteRecordFailForNsBadUserDepartment() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        TAUser user = getUser(TARole.ROLE_CONTROL_NS, false);
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        boolean result = regionSecurityService.check(user, regionRefBookId, uniqueRecordId);
        Assert.assertFalse(result);
    }

    @Test
    public void saveRecordSuccessForNs() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        TAUser user = getUser(TARole.ROLE_CONTROL_NS);
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        Long recordCommonId = COMMON_RECORD_ID;
        Date start = new Date();
        Date end = new Date();
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        RefBookValue regionValue = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION);
        values.put(REGION_ALIAS, regionValue);
        boolean result = regionSecurityService.check(user, regionRefBookId, uniqueRecordId, recordCommonId, values, start, end);

        Assert.assertTrue(result);
    }

    @Test
    public void saveRecordFailForNs() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        TAUser user = getUser(TARole.ROLE_CONTROL_NS);
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        Long recordCommonId = COMMON_RECORD_ID;
        Date start = new Date();
        Date end = new Date();
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        RefBookValue regionValue = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION + 1L);
        values.put(REGION_ALIAS, regionValue);
        boolean result = regionSecurityService.check(user, regionRefBookId, uniqueRecordId, recordCommonId, values, start, end);

        Assert.assertFalse(result);
    }

    @Test
    public void addRecordSuccessForNs() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        TAUser user = getUser(TARole.ROLE_CONTROL_NS);
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = null;
        Long recordCommonId = COMMON_RECORD_ID;
        Date start = new Date();
        Date end = new Date();
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        RefBookValue regionValue = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION);
        values.put(REGION_ALIAS, regionValue);
        boolean result = regionSecurityService.check(user, regionRefBookId, uniqueRecordId, recordCommonId, values, start, end);

        Assert.assertTrue(result);
    }

    /**
     * Получить пользователя с заданным параметром региона у его подразделения.
     *
     * @param roleAlias алиас роли пользователя
     */
    private TAUser getUser(String roleAlias) {
        return getUser(roleAlias, true);
    }

    /**
     * Получить пользователя.
     *
     * @param roleAlias алиас роли пользователя
     * @param hasRegionDepartment признак наличия региона у подразделяния пользователя
     */
    private TAUser getUser(String roleAlias, boolean hasRegionDepartment) {
        TAUser user = new TAUser();
        user.setId(1);

        List<TARole> roles = new ArrayList<TARole>();
        TARole role = new TARole();
        role.setId(1);
        role.setAlias(roleAlias);
        roles.add(role);

        user.setRoles(roles);

        user.setDepartmentId(hasRegionDepartment ? DEPARTAMENT_ID.intValue() : BAD_DEPARTAMENT_ID.intValue());
        return user;
    }
}
