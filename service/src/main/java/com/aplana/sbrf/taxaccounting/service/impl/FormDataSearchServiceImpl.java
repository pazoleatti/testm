package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter.AccessFilterType;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.util.FormTypeAlphanumericComparator;
import com.aplana.sbrf.taxaccounting.service.*;
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
	private FormTypeService formTypeService;

    @Autowired
    private FormDataAccessService formDataAccessService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private PeriodService reportPeriodService;

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
        if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_OPER)) {
            throw new AccessDeniedException("У пользователя нет прав на поиск по налоговым формам");
        }

        FormDataFilterAvailableValues result = new FormDataFilterAvailableValues();

        // Тип налоговой формы
        List<FormDataKind> kinds = new ArrayList<FormDataKind>(FormDataKind.values().length);
        kinds.add(null);
        kinds.addAll(formDataAccessService.getAvailableFormDataKind(userInfo, asList(taxType)));
        result.setKinds(kinds);

        // Вид налоговой формы
        TemplateFilter filter = new TemplateFilter();
        filter.setActive(true);
        filter.setTaxType(taxType);
        List<FormType> formTypesList = formTypeService.getByFilter(filter);
        Collections.sort(formTypesList, new FormTypeAlphanumericComparator());
        result.setFormType(formTypesList);

        // Подразделения
        // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
        result.setDepartmentIds(new HashSet<Integer>(departmentService.getTaxFormDepartments(userInfo.getUser(),
                asList(taxType))));

        // Подразделение по-умолчанию
        result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());

		return result;
	}

    @Override
    public List<FormType> getActiveFormTypeInReportPeriod(int reportPeriodId, TaxType taxType) {
        TemplateFilter filter = new TemplateFilter();
        filter.setActive(true);
        filter.setTaxType(taxType);
        List<FormType> formTypesList = formTypeService.getByFilter(filter);
        ReportPeriod reportPeriod = reportPeriodService.getReportPeriod(reportPeriodId);
        ArrayList<FormType> resultList = new ArrayList<FormType>();
        for (FormType type : formTypesList){
            for (FormTemplate formTemplate : formTemplateService.getFormTemplateVersionsByStatus(type.getId(), VersionedObjectStatus.NORMAL)){
                Date templateEndDate = formTemplateService.getFTEndDate(formTemplate.getId());
                if (templateEndDate != null){
                    if ((formTemplate.getVersion().compareTo(reportPeriod.getStartDate()) >= 0 && formTemplate.getVersion().compareTo(reportPeriod.getEndDate()) <= 0)
                            || (templateEndDate.compareTo(reportPeriod.getStartDate()) >= 0 && templateEndDate.compareTo(reportPeriod.getEndDate()) <= 0)
                            || (formTemplate.getVersion().compareTo(reportPeriod.getStartDate()) <=0 && templateEndDate.compareTo(reportPeriod.getEndDate()) >= 0)) {
                        resultList.add(type);
                    }
                }
                else{
                    if (formTemplate.getVersion().compareTo(reportPeriod.getStartDate()) <= 0
                            || formTemplate.getVersion().compareTo(reportPeriod.getEndDate()) <= 0)
                        resultList.add(type);
                }
            }
        }
        Collections.sort(resultList, new FormTypeAlphanumericComparator());
        return resultList;
    }

	@Override
	public List<FormType> getActiveFormTypeInReportPeriod(int departmentId, int reportPeriodId, TaxType taxType, TAUserInfo userInfo) {
		List<FormDataKind> kinds = new ArrayList<FormDataKind>(FormDataKind.values().length);
		kinds.addAll(formDataAccessService.getAvailableFormDataKind(userInfo, asList(taxType)));
		return formTypeService.getFormTypes(departmentId, reportPeriodId, taxType, kinds);
	}

    @Override
    public List<FormType> getActiveFormTypeInReportPeriod(int departmentId, int reportPeriodId, TaxType taxType, TAUserInfo userInfo, List<FormDataKind> kinds) {
        return formTypeService.getFormTypes(departmentId, reportPeriodId, taxType, kinds);
    }

    /**
     * Фильтр для Dao-слоя при выборке НФ. Пользовательская фильтрация и принудительная фильтрация.
     */
    private FormDataDaoFilter createFormDataDaoFilter(TAUserInfo userInfo, FormDataFilter formDataFilter) {
        FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();
        // ПОЛЬЗОВАТЕЛЬСКАЯ ФИЛЬТРАЦИЯ

        // Подразделения (могут быть не заданы - тогда все доступные по выборке 40 - http://conf.aplana.com/pages/viewpage.action?pageId=11380670)
        List<Integer> departments = formDataFilter.getDepartmentIds();
        if (departments == null || departments.isEmpty()) {
            departments = departmentService.getTaxFormDepartments(userInfo.getUser(),
                    formDataFilter.getTaxType() != null ? asList(formDataFilter.getTaxType()) : asList(TaxType.values()));
        }
        formDataDaoFilter.setDepartmentIds(departments);
        // Отчетные периоды
        formDataDaoFilter.setReportPeriodIds(formDataFilter.getReportPeriodIds());
        // Типы форм
        if (formDataFilter.getFormDataKind() != null) {
            List<FormDataKind> list = new ArrayList<FormDataKind>(FormDataKind.values().length);
            for(Long id: formDataFilter.getFormDataKind()){
                list.add(FormDataKind.fromId(id.intValue()));
            }
            formDataDaoFilter.setFormDataKind(list);
        } else {
            formDataDaoFilter.setFormDataKind(formDataAccessService.getAvailableFormDataKind(userInfo, asList(formDataFilter.getTaxType())));
        }
        // Вид форм
        formDataDaoFilter.setFormTypeIds(formDataFilter.getFormTypeId());
        // Состояние
        if (formDataFilter.getFormState() != null) {
            formDataDaoFilter.setStates(asList(formDataFilter.getFormState()));
        }
        // Признак возврата
        formDataDaoFilter.setReturnState(formDataFilter.getReturnState());
        // Вид налога
        if (formDataFilter.getTaxType() != null) {
            formDataDaoFilter.setTaxTypes(asList(formDataFilter.getTaxType()));
        }

        // ПРИНУДИТЕЛЬНАЯ ФИЛЬТРАЦИЯ

        // Добавляем условия для отбрасывания форм, на которые у пользователя нет прав доступа
        // Эти условия должны быть согласованы с реализацией в FormDataAccessServiceImpl
        if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
            // Контролер УНП без принудительной фильтрации
            formDataDaoFilter.setAccessFilterType(AccessFilterType.ALL);
        } else if (userInfo.getUser().hasRole(TARole.ROLE_OPER)) {
            // Операторы дополнительно фильтруются по подразделениям и типам форм
            formDataDaoFilter.setAccessFilterType(AccessFilterType.AVAILABLE_DEPARTMENTS_WITH_KIND);
            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
            formDataDaoFilter.setAvailableDepartmentIds(departmentService.getTaxFormDepartments(userInfo.getUser(),
                    asList(formDataFilter.getTaxType())));
            // Доступные типы
            List<FormDataKind> formDataKindList = new LinkedList<FormDataKind>();
            formDataKindList.add(FormDataKind.PRIMARY);
            if (formDataFilter.getTaxType() == TaxType.INCOME) {
                formDataKindList.add(FormDataKind.ADDITIONAL);
                formDataKindList.add(FormDataKind.UNP);
            }
            formDataDaoFilter.setAvailableFormDataKinds(formDataKindList);
        } else if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)) {
            // Контролеры дополнительно фильтруются по подразделениям
            formDataDaoFilter.setAccessFilterType(AccessFilterType.AVAILABLE_DEPARTMENTS);
            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
            formDataDaoFilter.setAvailableDepartmentIds(departmentService.getTaxFormDepartments(userInfo.getUser(),
                    formDataFilter.getTaxType() != null ? asList(formDataFilter.getTaxType()) : asList(TaxType.values())));
        } else {
            throw new AccessDeniedException("У пользователя нет прав на поиск по налоговым формам");
        }
        return formDataDaoFilter;
    }
}
