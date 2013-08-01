package com.aplana.sbrf.taxaccounting.dao.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentParamDaoTest.xml"})
@Deprecated
public class DepartmentParamDaoTest {
	@Autowired
	DepartmentParamDao departmentParamDao;
	
	private static final int DEPARTMENT_ID_FOR_TEST = 1;
	
	@Test
	@Transactional
	public void test(){
		DepartmentParam departmentParam =  departmentParamDao.getDepartmentParam(DEPARTMENT_ID_FOR_TEST);
		Assert.assertEquals(DEPARTMENT_ID_FOR_TEST, departmentParam.getDepartmentId());
		DepartmentParamIncome departmentParamIncome = departmentParamDao.getDepartmentParamIncome(DEPARTMENT_ID_FOR_TEST);
		Assert.assertEquals(DEPARTMENT_ID_FOR_TEST, departmentParamIncome.getDepartmentId());
		DepartmentParamTransport departmentParamTransport = departmentParamDao.getDepartmentParamTransport(DEPARTMENT_ID_FOR_TEST);
		Assert.assertEquals(DEPARTMENT_ID_FOR_TEST, departmentParamTransport.getDepartmentId());
	}

    @Test
    public void updateDepartmentParam()
    {
        DepartmentParam param = new DepartmentParam();
        param.setDepartmentId(DEPARTMENT_ID_FOR_TEST);
        param.setDictRegionId("01");
        param.setOkato("T");
        param.setInn("T");
        param.setKpp("T");
        param.setTaxOrganCode("T");
        param.setOkvedCode("T");
        param.setPhone("T");
        param.setReorgFormCode("T");
        param.setReorgInn("T");
        param.setReorgKpp("T");
        param.setName("T");

        departmentParamDao.updateDepartmentParam(param);

        Assert.assertEquals(departmentParamDao.getDepartmentParam(DEPARTMENT_ID_FOR_TEST), param);
    }

    @Test
    public void updateDepartmentParamIncome()
    {
        DepartmentParamIncome param = new DepartmentParamIncome();
        param.setDepartmentId(DEPARTMENT_ID_FOR_TEST);
        param.setSignatoryId(7);
        param.setSignatorySurname("T");
        param.setSignatoryFirstName("T");
        param.setSignatoryLastName("T");
        param.setApproveDocName("T");
        param.setApproveOrgName("T");
        param.setTaxPlaceTypeCode("213");
        param.setTaxRate(7d);
        param.setExternalTaxSum(7L);
        param.setSumDifference(7L);
        param.setCorrectionSum(7L);
        param.setAppVersion("T");
        param.setFormatVersion("T");

        departmentParamDao.updateDepartmentParamIncome(param);

        Assert.assertEquals(departmentParamDao.getDepartmentParamIncome(DEPARTMENT_ID_FOR_TEST), param);
    }

    @Test
    public void updateDepartmentParamTransport()
    {
        DepartmentParamTransport param = new DepartmentParamTransport();
        param.setDepartmentId(DEPARTMENT_ID_FOR_TEST);
        param.setSignatoryId(7);
        param.setSignatorySurname("T");
        param.setSignatoryFirstName("T");
        param.setSignatoryLastName("T");
        param.setApproveDocName("T");
        param.setApproveOrgName("T");
        param.setTaxPlaceTypeCode("213");
        param.setAppVersion("T");
        param.setFormatVersion("T");

        departmentParamDao.updateDepartmentParamTransport(param);

        Assert.assertEquals(departmentParamDao.getDepartmentParamTransport(DEPARTMENT_ID_FOR_TEST), param);
    }
}
