package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class PrintingServiceImplTest {

    private final static int USER_OPERATOR_ID = 14;
    private final static String USER_LOGIN = "controlBank";
    private final static int USER_DEPARTMENT_ID = 1;
    private final static String USER_ROLE = "ROLE_CONTROL_UNP";

    private List<LogSystemSearchResultItem> items = new ArrayList<LogSystemSearchResultItem>();

    private PrintingServiceImpl printingService =  new PrintingServiceImpl();

    @Before
    public void init(){

        TARole role = new TARole();
        role.setAlias(USER_ROLE);
        List<TARole> listUserRoles = new ArrayList<TARole>();
        listUserRoles.add(role);

        TAUser user = new TAUser();
        user.setId(USER_OPERATOR_ID);
        user.setLogin(USER_LOGIN);
        user.setEmail("controlBank@bank.ru");
        user.setDepartmentId(USER_DEPARTMENT_ID);
        user.setRoles(listUserRoles);
        user.setActive(true);

        LogSystemSearchResultItem item =  new LogSystemSearchResultItem();
        item.setUser(user);
        ReportPeriod rp =  new ReportPeriod();
        TaxPeriod taxPeriod = new TaxPeriod();
        taxPeriod.setStartDate(new Date());
        rp.setTaxPeriod(taxPeriod);
        rp.setName("1 квартал");
        item.setReportPeriod(rp);

        item.setEvent(FormDataEvent.ADD_ROW);
        item.setRoles(USER_ROLE);
        item.setFormKind(FormDataKind.ADDITIONAL);
        DeclarationType declarationType = new DeclarationType();
        declarationType.setName("РНУ");
        item.setDeclarationType(declarationType);

        FormType type = new FormType();
        type.setName("Сводная");
        item.setFormType(type);

        Department department = new Department();
        department.setName("Начальный");
        item.setDepartment(department);
        item.setLogDate(new Date());

        items.add(item);
    }

    @Test
    public void generateCsvTest(){
        File file = new File(printingService.generateAuditCsv(items));
        file.delete();
    }
}
