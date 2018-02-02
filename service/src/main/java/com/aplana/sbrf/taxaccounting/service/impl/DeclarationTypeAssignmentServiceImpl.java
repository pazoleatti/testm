package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationTypeAssignmentAction;
import com.aplana.sbrf.taxaccounting.model.action.EditDeclarationTypeAssignmentsAction;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.result.CreateDeclarationTypeAssignmentResult;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Сервис для работы с назначением налоговых форм {@link DeclarationTypeAssignment}
 */
@Service
@Transactional
public class DeclarationTypeAssignmentServiceImpl implements DeclarationTypeAssignmentService {
    private DepartmentService departmentService;
    private SourceService sourceService;
    private LogEntryService logEntryService;
    private DeclarationTypeService declarationTypeService;
    private DepartmentReportPeriodService departmentReportPeriodService;
    private DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

    public DeclarationTypeAssignmentServiceImpl(DepartmentService departmentService, SourceService sourceService, LogEntryService logEntryService,
                                                DeclarationTypeService declarationTypeService, DepartmentReportPeriodService departmentReportPeriodService,
                                                DepartmentDeclarationTypeDao departmentDeclarationTypeDao) {
        this.departmentService = departmentService;
        this.sourceService = sourceService;
        this.logEntryService = logEntryService;
        this.declarationTypeService = declarationTypeService;
        this.departmentReportPeriodService = departmentReportPeriodService;
        this.departmentDeclarationTypeDao = departmentDeclarationTypeDao;
    }

    /**
     * Получение страницы списка назначений налоговых форм подразделениям, доступным пользователю
     *
     * @param filter       Параметры фильтрации
     * @param userInfo     Информация о пользователе
     * @param pagingParams Параметры пагинации
     * @return Список назначений налоговых форм подразделениям {@link DeclarationTypeAssignment}
     */
    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_TAXES_NDFL_SETTINGS)")
    public PagingResult<DeclarationTypeAssignment> fetchDeclarationTypeAssignments(TAUserInfo userInfo, DeclarationTypeAssignmentFilter filter, PagingParams pagingParams) {
        List<Long> departmentIds = new ArrayList<>();

        if (filter.getDepartmentIds().isEmpty()) {
            for (Integer id : departmentService.getBADepartmentIds(userInfo.getUser())) {
                departmentIds.add(Long.valueOf(id));
            }
        } else {
            departmentIds.addAll(filter.getDepartmentIds());
        }

        List<DeclarationTypeAssignment> data = departmentDeclarationTypeDao.fetchDeclarationTypesAssignments(departmentIds, pagingParams);
        int count = departmentDeclarationTypeDao.fetchDeclarationTypesAssignmentsCount(departmentIds);

        return new PagingResult<>(data, count);
    }

    /**
     * Создание назначения налоговых форм подраздениям
     *
     * @param userInfo Информация о пользователе
     * @param action   Модель с данными
     * @return Результат назначения {@link CreateDeclarationTypeAssignmentResult}
     */
    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_DECLARATION_TYPES_ASSIGNMENT)")
    public CreateDeclarationTypeAssignmentResult createDeclarationTypeAssignment(TAUserInfo userInfo, CreateDeclarationTypeAssignmentAction action) {
        List<LogEntry> logs = new ArrayList<LogEntry>();
        boolean existingRelations = false;

        for (Integer depId : action.getDepartmentIds()) {
            for (Long dt : action.getDeclarationTypeIds()) {
                boolean canAssign = true;

                for (DepartmentDeclarationType ddt : sourceService.getDDTByDepartment(depId.intValue(), TaxType.NDFL, new Date(), new Date())) {
                    if (ddt.getDeclarationTypeId() == dt) {
                        existingRelations = true;
                        canAssign = false;
                        logs.add(new LogEntry(LogLevel.WARNING, "Для \"" + departmentService.getDepartment(depId).getName() +
                                "\" уже существует назначение \"" + declarationTypeService.get(ddt.getDeclarationTypeId()).getName() + "\""));
                    }
                }
                if (canAssign) {
                    List<Integer> childrenDepartmentIdList = departmentService.getAllChildrenIds(depId);
                    for (Integer childrenDepartmentId : childrenDepartmentIdList) {
                        addPeriod(childrenDepartmentId);
                    }
                    sourceService.saveDDT((long) depId, dt.intValue(), action.getPerformerIds());
                }
            }
        }

        CreateDeclarationTypeAssignmentResult result = new CreateDeclarationTypeAssignmentResult();
        result.setUuid(logEntryService.save(logs));
        result.setCreatingExistingRelations(existingRelations);

        return result;
    }

    /**
     * Редактирование назначений налоговых форм подраздениям. Выполняется изменение исполнителей у выбранных назначений
     *
     * @param userInfo Информация о пользователе
     * @param action   Модель с данными: назначения и исполнители {@link EditDeclarationTypeAssignmentsAction}
     * @return Результат назначения {@link CreateDeclarationTypeAssignmentResult}
     */
    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_DECLARATION_TYPES_ASSIGNMENT)")
    public void editDeclarationTypeAssignments(TAUserInfo userInfo, EditDeclarationTypeAssignmentsAction action) {
        for (Integer assignmentId : action.getAssignmentIds()) {
            sourceService.updateDDTPerformers(assignmentId, action.getPerformerIds());
        }
    }

    /**
     * Создание периодов для подразделения
     *
     * @param depId ID подразделения
     */
    private void addPeriod(int depId) {
        // Фильтр для проверки привязки подразделения к периоду
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(depId));

        List<DepartmentReportPeriod> currDepDrpList = departmentReportPeriodService.fetchAllByFilter(filter);

        // Находим все периоды для ПАО "Сбербанк"
        DepartmentReportPeriodFilter filterAll = new DepartmentReportPeriodFilter();
        filterAll.setDepartmentIdList(Arrays.asList(0));
        List<DepartmentReportPeriod> drpList = departmentReportPeriodService.fetchAllByFilter(filterAll);

        List<DepartmentReportPeriod> drpForSave = new LinkedList<DepartmentReportPeriod>(drpList);

        // Удаляем периоды имеющиеся у текущего подразеления
        outer:
        for (DepartmentReportPeriod drp : drpList) {
            for (DepartmentReportPeriod currDepDrp : currDepDrpList) {
                if (currDepDrp.getReportPeriod().getId().equals(drp.getReportPeriod().getId())
                        && currDepDrp.isActive() == drp.isActive()
                        && ((currDepDrp.getCorrectionDate() == null && drp.getCorrectionDate() == null) || (currDepDrp.getCorrectionDate() != null && currDepDrp.getCorrectionDate().equals(drp.getCorrectionDate())))) {
                    drpForSave.remove(drp);
                    continue outer;
                }
            }
        }

        for (DepartmentReportPeriod drp : drpForSave) {
            drp.setId(null);
            drp.setDepartmentId(depId);
            departmentReportPeriodService.create(drp);
        }
    }
}
