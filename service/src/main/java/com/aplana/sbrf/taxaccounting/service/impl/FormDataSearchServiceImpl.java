package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter.AccessFilterType;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.util.FormTypeAlphanumericComparator;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.asList;

@Service
public class FormDataSearchServiceImpl implements FormDataSearchService {

	@Autowired
	private FormDataSearchDao formDataSearchDao;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private FormTypeDao formTypeDao;

    @Autowired
    private SourceService sourceService;

	@Override
	public PagingResult<FormDataSearchResultItem> findDataByUserIdAndFilter(TAUserInfo userInfo, FormDataFilter formDataFilter) {
        return formDataSearchDao.findPage(createFormDataDaoFilter(userInfo, formDataFilter), formDataFilter.getSearchOrdering(),
				formDataFilter.isAscSorting(), new PagingParams(formDataFilter.getStartIndex(),
				formDataFilter.getCountOfRecords()));
	}

    @Override
    public List<Long> findDataIdsByUserAndFilter(TAUserInfo userInfo, FormDataFilter filter) {
        return formDataSearchDao.findIdsByFilter(createFormDataDaoFilter(userInfo, filter));
    }


    @Override
	public List<Department> listAllDepartmentsByParentDepartmentId(int parentDepartmentId) {
		List<Department> departmentList = new ArrayList<Department>();
		departmentList.add(departmentService.getDepartment(parentDepartmentId));
		departmentList.addAll(departmentService.getChildren(parentDepartmentId));
		return departmentList;
	}
	
	@Override
	public FormDataFilterAvailableValues getAvailableFilterValues(TAUserInfo userInfo, TaxType taxType) {
		FormDataFilterAvailableValues result = new FormDataFilterAvailableValues();
		
		if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
			// Контролёр УНП имеет доступ ко всем подразделениям
			result.setDepartmentIds(null);
			
			List<FormDataKind> kinds = new ArrayList<FormDataKind>(FormDataKind.values().length);
			kinds.addAll(asList(FormDataKind.values()));
			if (taxType != TaxType.INCOME) {
				// Выходные формы и формы УНП существуют только для налога на прибыль
				kinds.remove(FormDataKind.ADDITIONAL);
				kinds.remove(FormDataKind.UNP);
			}
            result.setKinds(kinds);
			
			// все виды налоговых форм по заданному виду налога
			List<FormType> formTypesList = formTypeDao.getByTaxType(taxType); 
			Collections.sort(formTypesList, new FormTypeAlphanumericComparator());
			result.setFormTypeIds(formTypesList);
			return result;
		}
		
		if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_OPER)) {
			throw new AccessDeniedException("У пользователя нет прав на поиск по налоговым формам");
		}
		
		// Собираем информацию о налоговых формах к которым имеет доступ пользователь
		// К формам своего подразделения имеет доступ и контролёр и оператор
		Set<DepartmentFormType> dfts = new HashSet(sourceService.getDFTByDepartment(
                userInfo.getUser().getDepartmentId(), taxType));

		// TODO Исправить после появления обновленной постановки на форму в 0.3.5 http://conf.aplana.com/pages/viewpage.action?pageId=11382061
		if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS) || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)) {
			 dfts.addAll(sourceService.getDFTSourcesByDepartment(userInfo.getUser().getDepartmentId(), taxType));
		}

		Map<Integer, FormType> formTypes = new HashMap<Integer, FormType>();
		Set<Integer> departmentIds = new HashSet<Integer>();
		Set<FormDataKind> kinds = new HashSet<FormDataKind>();
        for (DepartmentFormType dft : dfts) {
            int formTypeId = dft.getFormTypeId();
			if (!formTypes.containsKey(formTypeId)) {
                // TODO Очень неоптимально. Надо переписать.
				formTypes.put(formTypeId, formTypeDao.get(formTypeId));
			}

			kinds.add(dft.getKind());
			departmentIds.add(dft.getDepartmentId());
		}

        // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
        departmentIds.addAll(departmentService.getTaxFormDepartments(userInfo.getUser(), asList(taxType)));

		result.setDepartmentIds(departmentIds);
		
		List<FormType> formTypesList = new ArrayList<FormType>(formTypes.values());
		Collections.sort(formTypesList, new FormTypeAlphanumericComparator());
		result.setFormTypeIds(formTypesList);
		processKindListForCurrentUser(userInfo.getUser(), kinds);
		List<FormDataKind> kindsList = new ArrayList<FormDataKind>(kinds);
		Collections.sort(kindsList);
		result.setKinds(kindsList);
		result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());

		return result;
	}

	private void processKindListForCurrentUser(TAUser user, Set<FormDataKind> kindList){
		if(user.hasRole(TARole.ROLE_OPER)){
			kindList.remove(FormDataKind.SUMMARY);
		}
	}

    private FormDataDaoFilter createFormDataDaoFilter(TAUserInfo userInfo, FormDataFilter formDataFilter){
        FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();

        formDataDaoFilter.setDepartmentIds(formDataFilter.getDepartmentIds());
        formDataDaoFilter.setReportPeriodIds(formDataFilter.getReportPeriodIds());

        if(formDataFilter.getFormDataKind() == null){
            FormDataKind[] formDataKinds = FormDataKind.values();
            formDataDaoFilter.setFormDataKind(asList(formDataKinds));
        } else {
            formDataDaoFilter.setFormDataKind(asList(formDataFilter.getFormDataKind()));
        }

        if(formDataFilter.getFormTypeId() == null){
            formDataDaoFilter.setFormTypeIds(null);
        } else {
            formDataDaoFilter.setFormTypeIds(asList(formDataFilter.getFormTypeId()));
        }

        if(formDataFilter.getFormState() == null){
            WorkflowState[] formStates = WorkflowState.values();
            formDataDaoFilter.setStates(asList(formStates));
        } else {
            formDataDaoFilter.setStates(asList(formDataFilter.getFormState()));
        }

        formDataDaoFilter.setReturnState(formDataFilter.getReturnState());

        if(formDataFilter.getTaxType() == null){
            //В текущей реализации мы всегда идем по ветке else и сюда не попадаем, но  данное условие
            //добавлено, на случай, если в дальнейщем будет функциональность выбора по всем типам налога.
            TaxType[] taxTypes = TaxType.values();
            formDataDaoFilter.setTaxTypes(asList(taxTypes));
        } else {
            formDataDaoFilter.setTaxTypes(asList(formDataFilter.getTaxType()));
        }
        // Добавляем условия для отбрасывания форм, на которые у пользователя нет прав доступа
        // Эти условия должны быть согласованы с реализацией в FormDataAccessServiceImpl
        formDataDaoFilter.setUserDepartmentId(userInfo.getUser().getDepartmentId());
        if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            formDataDaoFilter.setAccessFilterType(AccessFilterType.ALL);
        } else if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL) || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)) {
            formDataDaoFilter.setAccessFilterType(AccessFilterType.USER_DEPARTMENT_AND_SOURCES);
        } else if (userInfo.getUser().hasRole(TARole.ROLE_OPER)) {
            formDataDaoFilter.setAccessFilterType(AccessFilterType.USER_DEPARTMENT);
        } else {
            throw new AccessDeniedException("У пользователя нет прав на поиск по налоговым формам");
        }

        return formDataDaoFilter;
    }
}
