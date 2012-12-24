package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
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
	public PaginatedSearchResult<FormDataSearchResultItem> findDataByUserIdAndFilter(TAUser user, FormDataFilter formDataFilter) {
		FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();

		if(formDataFilter.getDepartmentId() == null){
			List<Department> departmentList = listAllDepartmentsByParentDepartmentId(user.getDepartmentId());
			List<Integer> departmentIntegerList = new ArrayList<Integer>();
			for(Department department : departmentList){
				departmentIntegerList.add(department.getId());
			}
			formDataDaoFilter.setDepartmentIds(departmentIntegerList);
		} else {
			formDataDaoFilter.setDepartmentIds(Arrays.asList(formDataFilter.getDepartmentId()));
		}

		if(formDataFilter.getReportPeriodId() == null){
			List<ReportPeriod> reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(formDataFilter.getTaxType());
			List<Integer> reportPeriodIntegerList = new ArrayList<Integer>();
			for (ReportPeriod reportPeriod : reportPeriodList){
				reportPeriodIntegerList.add(reportPeriod.getId());
			}
			formDataDaoFilter.setReportPeriodIds(reportPeriodIntegerList);
		} else {
			formDataDaoFilter.setReportPeriodIds(Arrays.asList(formDataFilter.getReportPeriodId()));
		}

		if(formDataFilter.getFormDataKind() == null){
			FormDataKind[] formDataKinds = FormDataKind.values();
			formDataDaoFilter.setFormDataKind(Arrays.asList(formDataKinds));
		} else {
			formDataDaoFilter.setFormDataKind(Arrays.asList(formDataFilter.getFormDataKind()));
		}

		if(formDataFilter.getFormTypeId() == null){
			List<FormType> formTypeList = formTypeDao.listAllByTaxType(formDataFilter.getTaxType());
			List<Integer> formTypeIntegerList = new ArrayList<Integer>();

			for(FormType formType : formTypeList){
				formTypeIntegerList.add(formType.getId());
			}
			formDataDaoFilter.setFormTypeIds(formTypeIntegerList);
		} else {
			formDataDaoFilter.setFormTypeIds(Arrays.asList(formDataFilter.getFormTypeId()));
		}

		if(formDataFilter.getFormState() == null){
			WorkflowState[] formStates = WorkflowState.values();
			formDataDaoFilter.setStates(Arrays.asList(formStates));
		} else {
			formDataDaoFilter.setStates(Arrays.asList(formDataFilter.getFormState()));
		}

		if(formDataFilter.getTaxType() == null){
			//В текущей реализации мы всегда идем по ветке else и сюда не попадаем, но  данное условие
			//добавлено, на случай, если в дальнейщем будет функциональность выбора по всем типам налога.
			TaxType[] taxTypes = TaxType.values();
			formDataDaoFilter.setTaxTypes(Arrays.asList(taxTypes));
		} else {
			formDataDaoFilter.setTaxTypes(Arrays.asList(formDataFilter.getTaxType()));
		}

		return formDataSearchDao.findPage(formDataDaoFilter, formDataFilter.getSearchOrdering(),
				formDataFilter.isAscSorting(), new PaginatedSearchParams(formDataFilter.getStartIndex(),
				formDataFilter.getCountOfRecords()));
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
