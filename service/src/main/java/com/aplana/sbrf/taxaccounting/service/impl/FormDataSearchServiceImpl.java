package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.util.FormTypeAlphanumericComparator;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
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
    private SourceService sourceService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private FormDataDao formDataDao;

    @Override
    public Long getRowNumByFilter(TAUserInfo userInfo, FormDataFilter formDataFilter) {
        return formDataSearchDao.getRowNumByFilter(createFormDataDaoFilter(userInfo, formDataFilter), formDataFilter.getSearchOrdering(),
                formDataFilter.isAscSorting(), formDataFilter.getFormDataId());
    }

	@Override
	public PagingResult<FormDataSearchResultItem> findDataByUserIdAndFilter(TAUserInfo userInfo, FormDataFilter formDataFilter) {
        return formDataSearchDao.findPage(createFormDataDaoFilter(userInfo, formDataFilter), formDataFilter.getSearchOrdering(),
				formDataFilter.isAscSorting(), new PagingParams(formDataFilter.getStartIndex(),
				formDataFilter.getCountOfRecords()));
	}

    @SuppressWarnings("unchecked")
    @Override
    public List<FormData> findDataByFilter(FormDataFilter formDataFilter) {
        FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();
        formDataDaoFilter.setDepartmentIds(formDataFilter.getDepartmentIds());
        // Отчетные периоды
        formDataDaoFilter.setReportPeriodIds(formDataFilter.getReportPeriodIds());
        formDataDaoFilter.setComparativePeriodId(formDataFilter.getComparativePeriodId());
        if (formDataFilter.getFormDataKind() != null) {
            formDataDaoFilter.setFormDataKind((List<FormDataKind>) CollectionUtils.collect(formDataFilter.getFormDataKind(), new Transformer() {
                @Override
                public Object transform(Object input) {
                    return FormDataKind.fromId((Integer) input);
                }
            }));
        }
        formDataDaoFilter.setFormTypeIds(formDataFilter.getFormTypeId());
        // Состояние
        if (formDataFilter.getFormState() != null) {
            formDataDaoFilter.setStates(asList(formDataFilter.getFormState()));
        }
        // Признак возврата
        formDataDaoFilter.setReturnState(formDataFilter.getReturnState());
        // Признак корректирующего периорда
        formDataDaoFilter.setCorrectionTag(formDataFilter.getCorrectionTag());
        formDataDaoFilter.setCorrectionDate(formDataFilter.getCorrectionDate());
        // Вид налога
        if (formDataFilter.getTaxType() != null) {
            formDataDaoFilter.setTaxTypes(asList(formDataFilter.getTaxType()));
        }

        List<Long> ids = formDataSearchDao.findIdsByFilter(formDataDaoFilter);
        return ids.isEmpty() ? new ArrayList<FormData>(0) :
                (List<FormData>)CollectionUtils.collect(ids, new Transformer() {
                    @Override
                    public Object transform(Object input) {
                        return formDataDao.get((Long)input, null);
                    }
                });
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
        if (!userInfo.getUser().hasRole(TARole.N_ROLE_CONTROL_UNP)
                && !userInfo.getUser().hasRole(TARole.N_ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.N_ROLE_OPER)) {
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
        filter.setTaxType(taxType);
        List<FormType> formTypesList = formTypeService.getByFilter(filter);
        Collections.sort(formTypesList, new FormTypeAlphanumericComparator());
        result.setFormType(formTypesList);

        // Подразделения
        // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
        result.setDepartmentIds(new HashSet<Integer>(departmentService.getTaxFormDepartments(userInfo.getUser(),
                taxType, null, null)));

        // Подразделение по-умолчанию
        result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());

		return result;
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
        if (!userInfo.getUser().hasRole(TARole.N_ROLE_CONTROL_UNP)
                && !userInfo.getUser().hasRole(TARole.N_ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.N_ROLE_OPER)) {
            throw new AccessDeniedException("У пользователя нет прав на поиск по налоговым формам");
        }
        FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();
        // ПОЛЬЗОВАТЕЛЬСКАЯ ФИЛЬТРАЦИЯ

        // Подразделения (могут быть не заданы - тогда все доступные по выборке 40 - http://conf.aplana.com/pages/viewpage.action?pageId=11380670)
        Set<Integer> departments = new HashSet<Integer>();
        if (formDataFilter.getDepartmentIds() == null || formDataFilter.getDepartmentIds().isEmpty()) {
            if (formDataFilter.getTaxType() != null) {
                departments.addAll(departmentService.getTaxFormDepartments(userInfo.getUser(),
                        formDataFilter.getTaxType(), null, null));
            } else {
                // ToDo Налоговые формы выключены
                /*departments.addAll(departmentService.getTaxFormDepartments(userInfo.getUser(),
                        formDataFilter.getTaxType() != null ? formDataFilter.getTaxType() : asList(TaxType.values()), null, null));*/
            }

            // ПРИНУДИТЕЛЬНАЯ ФИЛЬТРАЦИЯ
            if (userInfo.getUser().hasRole(TARole.N_ROLE_OPER)) {
                // Операторы дополнительно фильтруются по подразделениям и типам форм
                // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
                departments.addAll(departmentService.getTaxFormDepartments(userInfo.getUser(),
                        formDataFilter.getTaxType(), null, null));
            }
        } else {
            departments.addAll(formDataFilter.getDepartmentIds());
        }

        // Отчетные периоды
        formDataDaoFilter.setReportPeriodIds(formDataFilter.getReportPeriodIds());
        // Типы форм
        Set<FormDataKind> kinds = new HashSet<FormDataKind>(FormDataKind.values().length);
        if (formDataFilter.getFormDataKind() == null || formDataFilter.getFormDataKind().isEmpty()) {
            kinds.addAll(formDataAccessService.getAvailableFormDataKind(userInfo, asList(formDataFilter.getTaxType())));
            // ПРИНУДИТЕЛЬНАЯ ФИЛЬТРАЦИЯ
            if (userInfo.getUser().hasRole(TARole.N_ROLE_OPER)) {
                // Доступные типы
                List<FormDataKind> formDataKindList = new LinkedList<FormDataKind>();
                formDataKindList.add(FormDataKind.PRIMARY);
                if (formDataFilter.getTaxType() == TaxType.INCOME) {
                    formDataKindList.add(FormDataKind.ADDITIONAL);
                    formDataKindList.add(FormDataKind.UNP);
                }
                kinds.addAll(formDataKindList);
            }
        } else {
            for (Long id: formDataFilter.getFormDataKind()){
                kinds.add(FormDataKind.fromId(id.intValue()));
            }
        }

        formDataDaoFilter.setDepartmentIds(new ArrayList<Integer>(departments));
        formDataDaoFilter.setFormDataKind(new ArrayList<FormDataKind>(kinds));

        // Виды форм
        TAUser tAUser = userInfo.getUser();
        List<Long> formTypes = formDataFilter.getFormTypeId();
        if (formTypes == null || formTypes.isEmpty()) {
            if (!tAUser.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
                if (tAUser.hasRole(TARole.N_ROLE_CONTROL_NS)) {
                    formTypes = sourceService.getDFTFormTypeBySource(tAUser.getDepartmentId(), formDataFilter.getTaxType(), formDataDaoFilter.getFormDataKinds());
                } else {
                    formTypes = sourceService.getDFTByPerformerDep(tAUser.getDepartmentId(), formDataFilter.getTaxType(), formDataDaoFilter.getFormDataKinds());
                }
                List<Department> departments10 = new ArrayList<Department>();
                if (tAUser.hasRole(TARole.N_ROLE_CONTROL_NS)) {
                    departments10 = departmentService.getBADepartments(tAUser, TaxType.NDFL);
                } else {
                    departments10.addAll(departmentService.getAllChildren(tAUser.getDepartmentId()));
                }
                Set<DepartmentFormType> departmentFormTypeList = new HashSet<DepartmentFormType>();
                for (Department department : departments10) {
                    if (formDataFilter.getReportPeriodIds().isEmpty()) {
                        departmentFormTypeList.addAll(sourceService.getDFTByDepartment(department.getId(), formDataFilter.getTaxType(), null, null));
                    } else {
                        for (Integer reportPeriodId : formDataFilter.getReportPeriodIds()) {
                            ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
                            departmentFormTypeList.addAll(sourceService.getDFTByDepartment(department.getId(), formDataFilter.getTaxType(),
                                    reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate()));
                        }
                    }
                }
                for(DepartmentFormType departmentFormType : departmentFormTypeList) {
                    formTypes.add((long)departmentFormType.getFormTypeId());
                }
                Set<Long> tFormTypes =  new HashSet<Long>(formTypes);
                formTypes.clear();
                formTypes.addAll(tFormTypes);
            }
        }
        formDataDaoFilter.setFormTypeIds(formTypes);

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
        // Признак корректирующего периода
        formDataDaoFilter.setCorrectionTag(formDataFilter.getCorrectionTag());
        return formDataDaoFilter;
    }
}
