package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.cache.CacheManagerDecorator;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationTypeAssignmentAction;
import com.aplana.sbrf.taxaccounting.model.action.EditDeclarationTypeAssignmentsAction;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CreateDeclarationTypeAssignmentResult;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.collections4.CollectionUtils;
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
    private DeclarationDataService declarationDataService;
    private DepartmentReportPeriodService departmentReportPeriodService;
    private DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
    private CacheManagerDecorator cacheManagerDecorator;
    private TAUserService userService;

    public DeclarationTypeAssignmentServiceImpl(DepartmentService departmentService, SourceService sourceService, LogEntryService logEntryService,
                                                DeclarationTypeService declarationTypeService, DeclarationDataService declarationDataService,
                                                DepartmentReportPeriodService departmentReportPeriodService, DepartmentDeclarationTypeDao departmentDeclarationTypeDao,
                                                CacheManagerDecorator cacheManagerDecorator, TAUserService userService) {
        this.departmentService = departmentService;
        this.sourceService = sourceService;
        this.logEntryService = logEntryService;
        this.declarationTypeService = declarationTypeService;
        this.declarationDataService = declarationDataService;
        this.departmentReportPeriodService = departmentReportPeriodService;
        this.departmentDeclarationTypeDao = departmentDeclarationTypeDao;
        this.cacheManagerDecorator = cacheManagerDecorator;
        this.userService = userService;
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
        List<LogEntry> logs = new ArrayList<>();

        // Пользователь пытается создать назначения, которые уже существуют
        boolean existingRelations = false;

        // Создание назначений для всех указанных подразделений и видов налоговых форм
        for (Integer depId : action.getDepartmentIds()) {
            for (Long dt : action.getDeclarationTypeIds()) {
                // Можно ли создать назначение
                boolean canAssign = true;

                // Выполняется поиск назначений для подразделения. Если среди найденных назначений есть назначение формы типа, который
                // в данный момент пытаемся назначить, то это назначение не выполнять и сообщить пользователю, что назначение существует
                for (DepartmentDeclarationType ddt : sourceService.getDDTByDepartment(depId.intValue(), TaxType.NDFL, new Date(), new Date())) {
                    if (ddt.getDeclarationTypeId() == dt) {
                        existingRelations = true;
                        canAssign = false;
                        logs.add(new LogEntry(LogLevel.WARNING, "Для \"" + departmentService.getDepartment(depId).getName() +
                                "\" уже существует назначение \"" + declarationTypeService.get(ddt.getDeclarationTypeId()).getName() + "\""));
                    }
                }

                // Существующих назначений не найдено, назначить
                if (canAssign) {
                    // Создать дочерним подразделениям отчетные периоды, которых у них нет, но есть у ПАО "Сбербанк"
                    List<Integer> childrenDepartmentIdList = departmentService.getAllChildrenIds(depId);
                    for (Integer childrenDepartmentId : childrenDepartmentIdList) {
                        addAllReportPeriodsToDepartment(childrenDepartmentId);
                    }
                    sourceService.saveDDT((long) depId, dt.intValue(), action.getPerformerIds());

                    // Сброс кэша для получения доступных подразделений пользователя (учитываются исполнители)
                    // Перебираем пользователей, т.к в этом же кэше хранятся и сами подразделения, которые не изменились
                    for (TAUser user : userService.listAllUsers()) {
                        cacheManagerDecorator.evict(CacheConstants.DEPARTMENT, "user_departments_" + user.getId());
                    }
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
     */
    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_DECLARATION_TYPES_ASSIGNMENT)")
    public void editDeclarationTypeAssignments(TAUserInfo userInfo, EditDeclarationTypeAssignmentsAction action) {
        if (!CollectionUtils.isEmpty(action.getAssignmentIds())) {
            for (Integer assignmentId : action.getAssignmentIds()) {
                sourceService.updateDDTPerformers(assignmentId, action.getPerformerIds());
            }

            // Сброс кэша для получения доступных подразделений пользователя (учитываются исполнители)
            // Перебираем пользователей, т.к в этом же кэше хранятся и сами подразделения, которые не изменились
            for (TAUser user : userService.listAllUsers()) {
                cacheManagerDecorator.evict(CacheConstants.DEPARTMENT, "user_departments_" + user.getId());
            }
        }
    }

    /**
     * Отмена назначений налоговых форм подраздениям
     *
     * @param userInfo    Информация о пользователе
     * @param assignments Список упрощенных моделей назначений {@link DeclarationTypeAssignmentIdModel}
     * @return Результат отмены назначения {@link ActionResult}
     */
    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EDIT_DECLARATION_TYPES_ASSIGNMENT)")
    public ActionResult deleteDeclarationTypeAssignments(TAUserInfo userInfo, List<DeclarationTypeAssignmentIdModel> assignments) {
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        boolean declarationsExist = false;

        if (!CollectionUtils.isEmpty(assignments)) {
            for (DeclarationTypeAssignmentIdModel assignment : assignments) {
                declarationsExist |= declarationDataService.existDeclaration(assignment.getDeclarationTypeId(), assignment.getDepartmentId(), logger.getEntries());
                if (!declarationsExist) {
                    sourceService.deleteDDT(Collections.singletonList(assignment.getId()));
                }
            }

            // Сброс кэша для получения доступных подразделений пользователя (учитываются исполнители)
            // Перебираем пользователей, т.к в этом же кэше хранятся и сами подразделения, которые не изменились
            for (TAUser user : userService.listAllUsers()) {
                cacheManagerDecorator.evict(CacheConstants.DEPARTMENT, "user_departments_" + user.getId());
            }
        }

        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    /**
     * Создание отчетных периодов для подразделения. Создаются все периоды, которые есть у останых подразделений,
     * например, у ПАО "Сбербанк", но которых нет у заданного подразделения
     *
     * @param depId ID подразделения
     */
    private void addAllReportPeriodsToDepartment(int depId) {
        // Поиск периодов заданного подразделения
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(depId));
        List<DepartmentReportPeriod> depDrpList = departmentReportPeriodService.fetchAllByFilter(filter);

        // Поиск периодов ПАО "Сбербанк"
        DepartmentReportPeriodFilter filterAll = new DepartmentReportPeriodFilter();
        filterAll.setDepartmentIdList(Arrays.asList(Department.ROOT_DEPARTMENT_ID));
        List<DepartmentReportPeriod> bankDrpList = departmentReportPeriodService.fetchAllByFilter(filterAll);

        // Список отчетных периодов подразделения для добавления заданному подразделению
        // Строится на основе списка периодов ПАО "Сбербанк", из него исключаются те периоды,
        // которые уже есть у заданного подразделения
        List<DepartmentReportPeriod> drpForCreate = new LinkedList<>(bankDrpList);

        // Удаление периодов, имеющихся у заданного подразеления. Чтобы не создавалось то, что уже существует
        // Перебираются все периоды ПАО "Сбербанк", если у заданного подразделения находится равный ему период,
        // то он удаляется из списка периодов для создания
        for (DepartmentReportPeriod bankDrp : bankDrpList) {
            for (DepartmentReportPeriod depDrp : depDrpList) {
                if (periodsAreEqual(depDrp, bankDrp)) {
                    drpForCreate.remove(bankDrp);
                    break;
                }
            }
        }

        //Создание отчетных периодов у заданного подразделения
        for (DepartmentReportPeriod drp : drpForCreate) {
            drp.setId(null);
            drp.setDepartmentId(depId);
            departmentReportPeriodService.create(drp);
        }
    }

    /**
     * Отчетные периоды подразделений совпадают по id отчетных периодов, активности, дате сдачи корректировки
     *
     * @param period1 Отчетный период 1-го подразделения
     * @param period2 Отчетный период 2-го подразделения
     * @return
     */
    private boolean periodsAreEqual(DepartmentReportPeriod period1, DepartmentReportPeriod period2) {
        return period1.getReportPeriod().getId().equals(period2.getReportPeriod().getId())
                && period1.isActive() == period2.isActive()
                && ((period1.getCorrectionDate() == null && period2.getCorrectionDate() == null) || (period1.getCorrectionDate() != null && period1.getCorrectionDate().equals(period2.getCorrectionDate())));
    }
}
