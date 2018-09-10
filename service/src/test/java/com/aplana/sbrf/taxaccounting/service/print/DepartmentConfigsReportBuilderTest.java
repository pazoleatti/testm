package com.aplana.sbrf.taxaccounting.service.print;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPresentPlace;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookReorganization;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSignatoryMark;
import com.aplana.sbrf.taxaccounting.service.impl.print.departmentConfigs.DepartmentConfigsReportBuilder;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DepartmentConfigsReportBuilderTest {

    @Test
    public void test() throws Exception {
        List<DepartmentConfig> departmentConfigs = new ArrayList<>();
        departmentConfigs.add(departmentConfig1());
        departmentConfigs.add(departmentConfig2());

        DepartmentConfigsReportBuilder reportBuilder = new DepartmentConfigsReportBuilder(departmentConfigs, department());
        String filepath = null;
        try {
            filepath = reportBuilder.createReport();
        } finally {
            if (filepath != null) {
                new File(filepath).delete();
            }
        }
    }

    private DepartmentConfig departmentConfig1() {
        DepartmentConfig departmentConfig = new DepartmentConfig();
        departmentConfig.setStartDate(new LocalDate(2018, 1, 1).toDate());
        departmentConfig.setEndDate(new LocalDate(2018, 12, 31).toDate());
        departmentConfig.setKpp("kpp");
        departmentConfig.setOktmo(oktmo());
        departmentConfig.setTaxOrganCode("taxOrganCode");
        departmentConfig.setPresentPlace(presentPlace());
        departmentConfig.setName("name");
        departmentConfig.setPhone("phone");
        departmentConfig.setSignatoryMark(signatoryMark());
        departmentConfig.setSignatorySurName("signatoryMarkSurName");
        departmentConfig.setSignatoryFirstName("signatoryMarkFirstName");
        departmentConfig.setSignatoryLastName("signatoryMarkLastName");
        departmentConfig.setApproveDocName("approveDocName");
        departmentConfig.setReorganization(reorganization());
        departmentConfig.setReorgKpp("reorgKpp");
        departmentConfig.setReorgInn("reorgInn");
        return departmentConfig;
    }

    private RefBookOktmo oktmo() {
        RefBookOktmo oktmo = new RefBookOktmo();
        oktmo.setCode("oktmoCode");
        oktmo.setName("oktmoName");
        return oktmo;
    }

    private RefBookPresentPlace presentPlace() {
        RefBookPresentPlace presentPlace = new RefBookPresentPlace();
        presentPlace.setCode("presentPlaceCode");
        presentPlace.setName("presentPlaceName");
        return presentPlace;
    }

    private RefBookSignatoryMark signatoryMark() {
        RefBookSignatoryMark signatoryMark = new RefBookSignatoryMark();
        signatoryMark.setCode(1);
        signatoryMark.setName("signatoryMarkName");
        return signatoryMark;
    }

    private RefBookReorganization reorganization() {
        RefBookReorganization reorganization = new RefBookReorganization();
        reorganization.setCode("reorganizationCode");
        reorganization.setName("reorganizationName");
        return reorganization;
    }

    private DepartmentConfig departmentConfig2() {
        DepartmentConfig departmentConfig = new DepartmentConfig();
        departmentConfig.setStartDate(new Date());
        return departmentConfig;
    }

    private Department department() {
        Department department = new Department();
        department.setShortName("depShortName3456789012345678901234567890");
        return department;
    }
}
