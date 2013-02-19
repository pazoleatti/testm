package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter.AccessFilterType;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;

@Service
public class FormDataSearchServiceImpl implements FormDataSearchService {

	@Autowired
	private FormDataSearchDao formDataSearchDao;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private FormTypeDao formTypeDao;

    @Autowired
    private TAUserDao taUserDao;

	@Override
	public PaginatedSearchResult<FormDataSearchResultItem> findDataByUserIdAndFilter(TAUser user, FormDataFilter formDataFilter) {
		FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();

		formDataDaoFilter.setDepartmentIds(formDataFilter.getDepartmentId());
		formDataDaoFilter.setReportPeriodIds(formDataFilter.getReportPeriodIds());

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
		
		// Добавляем условия для отбрасывания форм, на которые у пользователя нет прав доступа
		// Эти условия должны быть согласованы с реализацией в FormDataAccessServiceImpl		
		formDataDaoFilter.setUserDepartmentId(user.getDepartmentId());
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)) {
			formDataDaoFilter.setAccessFilterType(AccessFilterType.ALL);
		} else if (user.hasRole(TARole.ROLE_CONTROL)) {
			formDataDaoFilter.setAccessFilterType(AccessFilterType.USER_DEPARTMENT_AND_SOURCES);
		} else if (user.hasRole(TARole.ROLE_OPERATOR)) {
			formDataDaoFilter.setAccessFilterType(AccessFilterType.USER_DEPARTMENT);
		} else {
			throw new AccessDeniedException("У пользователя нет прав на поиск по налоговым формам");
		}

		return formDataSearchDao.findPage(formDataDaoFilter, formDataFilter.getSearchOrdering(),
				formDataFilter.isAscSorting(), new PaginatedSearchParams(formDataFilter.getStartIndex(),
				formDataFilter.getCountOfRecords()));
	}

	@Override
	public List<Department> listAllDepartmentsByParentDepartmentId(int parentDepartmentId) {
		List<Department> departmentList = new ArrayList<Department>();
		departmentList.add(departmentService.getDepartment(parentDepartmentId));
		departmentList.addAll(departmentService.getChildren(parentDepartmentId));
		return departmentList;
	}

	@Override
	public List<FormType> getAvailableFormTypes(int userId, TaxType taxType){
		return formTypeDao.listAllByDepartmentIdAndTaxType(taUserDao.getUser(userId).getDepartmentId(), taxType);
	}

	@Override
	public List<Department> getAvailableDepartments(int userId, TaxType taxType) {
		throw new UnsupportedOperationException("not implemented");
	}
}
