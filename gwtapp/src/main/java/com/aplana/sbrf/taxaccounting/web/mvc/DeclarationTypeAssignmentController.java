package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeAssignmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для работы с назначением налоговых форм
 */

@RestController
public class DeclarationTypeAssignmentController {
    private DeclarationTypeAssignmentService assignmentService;
    private SecurityService securityService;

    public DeclarationTypeAssignmentController(DeclarationTypeAssignmentService assignmentService, SecurityService securityService) {
        this.assignmentService = assignmentService;
        this.securityService = securityService;
    }

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(DeclarationTypeAssignmentFilter.class, new RequestParamEditor(DeclarationTypeAssignmentFilter.class));
    }

    /**
     * Получение списка назначений
     *
     * @param filter       Значения фильтра
     * @param pagingParams Параметры для пагинации
     * @return Список назначений {@link FormTypeKind}
     */
    @GetMapping(value = "/rest/declarationTypeAssignment")
    public JqgridPagedList<FormTypeKind> fetchDeclarationTypeAssignments(@RequestParam DeclarationTypeAssignmentFilter filter, @RequestParam PagingParams pagingParams) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        PagingResult<FormTypeKind> pagingResult = assignmentService.fetchDeclarationTypeAssignments(userInfo, filter, pagingParams);

        return JqgridPagedResourceAssembler.buildPagedList(
                pagingResult,
                pagingResult.getTotalCount(),
                pagingParams
        );
    }
}