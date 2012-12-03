package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FormDataSearchServiceImpl implements FormDataSearchService {

	@Autowired
	private FormDataSearchDao formDataSearchDao;

	@Autowired
	private DepartmentDao departmentDao;

	@Autowired
	private FormTypeDao formTypeDao;

	@Autowired
	private ReportPeriodDao reportPeriodDao;


	@Override
	public List<FormDataSearchResultItem> findDataByUserIdAndFilter(TAUser user, FormDataFilter formDataFilter) {
		FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();

		if(formDataFilter.getDepartment() == Long.MAX_VALUE){
			List<Department> departmentList = listAllDepartmentsByParentDepartmentId(user.getDepartmentId());
			List<Long> departmentLongList = new ArrayList<Long>();
			for(Department department : departmentList){
				departmentLongList.add((long)department.getId());
			}
			formDataDaoFilter.setDepartment(departmentLongList);
		} else {
			formDataDaoFilter.setDepartment(Arrays.asList(formDataFilter.getDepartment()));
		}

		if(formDataFilter.getPeriod() == Long.MAX_VALUE){
			List<ReportPeriod> reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(formDataFilter.getTaxType());
			List<Long> reportPeriodLongList = new ArrayList<Long>();
			for (ReportPeriod reportPeriod : reportPeriodList){
				reportPeriodLongList.add((long)reportPeriod.getId());
			}
			formDataDaoFilter.setPeriod(reportPeriodLongList);
		} else {
			formDataDaoFilter.setPeriod(Arrays.asList(formDataFilter.getPeriod()));
		}

		if(formDataFilter.getKind() == Long.MAX_VALUE){
			FormDataKind[] formDataKinds = FormDataKind.values();
			List<Long> formDataKindLongList = new ArrayList<Long>();
			for(int i = 0; i < formDataKinds.length; i++){
				formDataKindLongList.add((long)formDataKinds[i].getId());
			}
			formDataDaoFilter.setKind(formDataKindLongList);
		} else {
			formDataDaoFilter.setKind(Arrays.asList(formDataFilter.getKind()));
		}

		if(formDataFilter.getFormtype() == Long.MAX_VALUE){
			List<FormType> formTypeList = formTypeDao.listAllByTaxType(formDataFilter.getTaxType());
			List<Long> formTypeLongList = new ArrayList<Long>();

			for(FormType formType : formTypeList){
				formTypeLongList.add((long)formType.getId());
			}
			formDataDaoFilter.setFormtype(formTypeLongList);
		} else {
			formDataDaoFilter.setFormtype(Arrays.asList(formDataFilter.getFormtype()));
		}

		if(formDataFilter.getFormState() == Long.MAX_VALUE){
			WorkflowState[] formStates = WorkflowState.values();
			List<Long> formStatesLongList = new ArrayList<Long>();
			for(int i = 0; i < formStates.length; i++){
				formStatesLongList.add((long)formStates[i].getId());
			}
			formDataDaoFilter.setFormStates(formStatesLongList);
		} else {
			formDataDaoFilter.setFormStates(Arrays.asList(formDataFilter.getFormState()));
		}

		return formDataSearchDao.findByFilter(formDataDaoFilter);
	}

	@Override
	public List<Department> listAllDepartmentsByParentDepartmentId(int parentDepartmentId) {
		List<Department> departmentList = new ArrayList<Department>();
		departmentList.add(departmentDao.getDepartment(parentDepartmentId));
		departmentList.addAll(departmentDao.getChildren(parentDepartmentId));
		return departmentList;
	}

	@Override
	public List<FormType> listFormTypes() {
		return formTypeDao.listFormTypes();
	}

	@Override
	public List<FormType> listFormTypesByTaxType(TaxType taxType){
		return formTypeDao.listAllByTaxType(taxType);
	}

	@Override
	public List<ReportPeriod> listReportPeriodsByTaxType(TaxType taxType) {
		return reportPeriodDao.listAllPeriodsByTaxType(taxType);
	}

}
