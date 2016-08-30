package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RefBookDepartmentTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    RefBookDepartment refBookDepartment = new RefBookDepartment();

    DepartmentService departmentService = mock(DepartmentService.class);
    RefBookDao refBookDao = mock(RefBookDao.class);
    LockDataService lockService = mock(LockDataService.class);

    @Before
    public void setup() {
        ReflectionTestUtils.setField(refBookDepartment, "departmentService", departmentService);
        ReflectionTestUtils.setField(refBookDepartment, "refBookDao", refBookDao);
        ReflectionTestUtils.setField(refBookDepartment, "lockService", lockService);
        RefBookFactory refBookFactory = mock(RefBookFactoryImpl.class);
        ReflectionTestUtils.setField(refBookDepartment, "refBookFactory", refBookFactory);

        RefBook refBook = mock(RefBook.class);
        when(refBookDao.get(anyLong())).thenReturn(refBook);

        when(lockService.lock(anyString(), anyInt(), anyString(), anyString())).thenReturn(null);
    }

    /**
     * Проверка типа удаляемого подразделения
     *
     * @throws Exception
     */
    @Test
    public void testDeleteRecordVersionsWhenDepartmentTypeIsRootBank() throws Exception {
        expectedException.expect(ServiceLoggerException.class);
        expectedException.expectMessage("Подразделение не может быть удалено, так как оно имеет тип \"Банк\"!");

        Department department = new Department();
        department.setType(DepartmentType.ROOT_BANK);
        when(departmentService.getDepartment(0)).thenReturn(department);

        TAUserInfo taUserInfo = new TAUserInfo();
        taUserInfo.setUser(mock(TAUser.class));

        Logger logger = new Logger();
        logger.setTaUserInfo(taUserInfo);

        List<Long> list = new ArrayList<Long>();
        list.add(0L);

        refBookDepartment.deleteRecordVersions(logger, list, true);
    }
}