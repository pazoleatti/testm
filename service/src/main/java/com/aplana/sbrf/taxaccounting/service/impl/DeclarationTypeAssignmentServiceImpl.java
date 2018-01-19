package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeAssignmentService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с назначением налоговых форм
 */
@Service
@Transactional
public class DeclarationTypeAssignmentServiceImpl implements DeclarationTypeAssignmentService {
    private DepartmentService departmentService;
    private SourceService sourceService;

    public DeclarationTypeAssignmentServiceImpl(DepartmentService departmentService, SourceService sourceService) {
        this.departmentService = departmentService;
        this.sourceService = sourceService;
    }

    /**
     * Получение списка назначений налоговых форм подразделениям, доступным пользователю
     *
     * @param userInfo     Информация о пользователе
     * @param pagingParams Параметры пагинации
     * @return Список назначений
     */
    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_TAXES_NDFL_SETTINGS)")
    public PagingResult<FormTypeKind> fetchDeclarationTypeAssignments(TAUserInfo userInfo, PagingParams pagingParams) {
        List<Long> departmentsIds = new ArrayList<Long>();
        for (Integer id : departmentService.getBADepartmentIds(userInfo.getUser())) {
            departmentsIds.add(Long.valueOf(id));
        }

        PagingResult<FormTypeKind> result = sourceService.fetchAssignedDeclarationTypes(departmentsIds, pagingParams);

        return result;
    }
}
