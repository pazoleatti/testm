package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
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
    private final Long UNIQUE_RECORD_ID_BAD_VERSION = 2L;
    private final Long COMMON_RECORD_ID = 1L;
    private final Long PREV_RECORD_ID = 2L;

    private final Long DEPARTAMENT_ID = 1L;
    private final Long BAD_DEPARTAMENT_ID = 2L;

    private final Date START = new Date();

    private TAUser nsUser = getUser(TARole.N_ROLE_CONTROL_NS);

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

        // версии записи - правильные (принадлежат к одному региону и их можнет удалить один контролер НС)
        // и неправильные (контролер НС можнет удалить только некоторые версии, но не всю запись со всеми версиями записи)
        PagingResult<Map<String, RefBookValue>> versionsGood = new PagingResult<Map<String, RefBookValue>>();
        PagingResult<Map<String, RefBookValue>> versionsBad = new PagingResult<Map<String, RefBookValue>>();
        Map<String, RefBookValue> version1 = new HashMap<String, RefBookValue>();
        Map<String, RefBookValue> version2 = new HashMap<String, RefBookValue>();
        Map<String, RefBookValue> versionBad = new HashMap<String, RefBookValue>();
        RefBookValue regionTmp1 = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION);
        RefBookValue regionTmp2 = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION);
        RefBookValue regionTmpBad = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION + 1);
        version1.put(REGION_ALIAS, regionTmp1);
        version2.put(REGION_ALIAS, regionTmp2);
        versionBad.put(REGION_ALIAS, regionTmpBad);
        versionsGood.add(version1);
        versionsGood.add(version2);
        versionsBad.add(version1);
        versionsBad.add(versionBad);

        RefBookFactory refBookFactory = mock(RefBookFactory.class);
        ReflectionTestUtils.setField(regionSecurityService, "refBookFactory", refBookFactory);
        DepartmentService departmentService = mock(DepartmentService.class);
        ReflectionTestUtils.setField(regionSecurityService, "departmentService", departmentService);

        List<Department> departments = new ArrayList<Department>();
        Department nsDepartment = new Department();
        nsDepartment.setRegionId(USER_REGION);
        departments.add(nsDepartment);
        when(departmentService.getBADepartments(nsUser, TaxType.NDFL)).thenReturn(departments);

        when(refBookFactory.get(NOT_REGION_REF_BOOK_ID)).thenReturn(notRegionRefBook);
        when(refBookFactory.get(REGION_REF_BOOK_ID)).thenReturn(refBook);

        when(refBookFactory.getDataProvider(RefBook.Id.DEPARTMENT.getId())).thenReturn(departmentProvider);
        when(departmentProvider.getRecordData(DEPARTAMENT_ID)).thenReturn(department);
        when(departmentProvider.getRecordData(BAD_DEPARTAMENT_ID)).thenReturn(badDepartment);

        when(refBookFactory.getDataProvider(NOT_REGION_REF_BOOK_ID)).thenReturn(provider);
        when(refBookFactory.getDataProvider(REGION_REF_BOOK_ID)).thenReturn(provider);
        when(provider.getRecordData(UNIQUE_RECORD_ID)).thenReturn(record);
        when(provider.getRecordData(PREV_RECORD_ID)).thenReturn(record);

        when(provider.getRecordIdPairs(REGION_REF_BOOK_ID, START, false, null)).thenReturn(pairs);
        when(provider.getRecordsVersionStart(list)).thenReturn(versionDateMap);

        when(provider.getRecordVersionsById(UNIQUE_RECORD_ID, null, null, null)).thenReturn(versionsGood);
        when(provider.getRecordVersionsById(UNIQUE_RECORD_ID_BAD_VERSION, null, null, null)).thenReturn(versionsBad);
    }

    @Test
    public void deleteRecordSuccessForUnp() {
        // для роли UNP всегда можно изменять
        TAUser user = getUser(TARole.N_ROLE_CONTROL_UNP);
        Long regionRefBook = REGION_REF_BOOK_ID;
        Long uniqueRecord = UNIQUE_RECORD_ID;
        boolean result = regionSecurityService.checkDelete(user, regionRefBook, uniqueRecord, true);
        Assert.assertTrue(result);

        Long notRegionRefBook = NOT_REGION_REF_BOOK_ID;
        result = regionSecurityService.checkDelete(user, notRegionRefBook, uniqueRecord, true);
        Assert.assertTrue(result);
    }

    @Test
    public void deleteRecordForNs() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        boolean result = regionSecurityService.checkDelete(nsUser, regionRefBookId, uniqueRecordId, true);
        Assert.assertTrue(result);

        regionRefBookId = NOT_REGION_REF_BOOK_ID;
        result = regionSecurityService.checkDelete(nsUser, regionRefBookId, uniqueRecordId, true);
        Assert.assertFalse(result);
    }

    @Test
    public void deleteRecordFailForNsBadUserDepartment() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        TAUser user = getUser(TARole.N_ROLE_CONTROL_NS, false);
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        boolean result = regionSecurityService.checkDelete(user, regionRefBookId, uniqueRecordId, true);
        Assert.assertFalse(result);
    }

    @Test
    public void deleteRecordForNsGoodVersions() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        boolean result = regionSecurityService.checkDelete(nsUser, regionRefBookId, uniqueRecordId, false);
        Assert.assertTrue(result);
    }

    @Test
    public void deleteRecordFailForNsBadVersions() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID_BAD_VERSION;
        boolean result = regionSecurityService.checkDelete(nsUser, regionRefBookId, uniqueRecordId, false);
        Assert.assertFalse(result);
    }

    @Test
    public void saveRecordSuccessForNs() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = UNIQUE_RECORD_ID;
        Long recordCommonId = COMMON_RECORD_ID;
        Date start = new Date();
        Date end = new Date();
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        RefBookValue regionValue = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION);
        values.put(REGION_ALIAS, regionValue);
        boolean result = regionSecurityService.check(nsUser, regionRefBookId, uniqueRecordId, recordCommonId, values, start, end);

        Assert.assertTrue(result);
    }

    @Test
    public void saveRecordFailForNs() {
        // для ролей NS можно править только региональные записи относящиеся к его региону
        TAUser user = getUser(TARole.N_ROLE_CONTROL_NS);
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
        Long regionRefBookId = REGION_REF_BOOK_ID;
        Long uniqueRecordId = null;
        Long recordCommonId = COMMON_RECORD_ID;
        Date start = new Date();
        Date end = new Date();
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        RefBookValue regionValue = new RefBookValue(RefBookAttributeType.REFERENCE, USER_REGION);
        values.put(REGION_ALIAS, regionValue);
        boolean result = regionSecurityService.check(nsUser, regionRefBookId, uniqueRecordId, recordCommonId, values, start, end);

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
        role.setTaxType(TaxType.NDFL);
        roles.add(role);

        user.setRoles(roles);

        user.setDepartmentId(hasRegionDepartment ? DEPARTAMENT_ID.intValue() : BAD_DEPARTAMENT_ID.intValue());
        return user;
    }
}
