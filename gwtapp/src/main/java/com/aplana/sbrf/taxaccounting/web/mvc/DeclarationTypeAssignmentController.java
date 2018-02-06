package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationTypeAssignmentAction;
import com.aplana.sbrf.taxaccounting.model.action.EditDeclarationTypeAssignmentsAction;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.CreateDeclarationTypeAssignmentResult;
import com.aplana.sbrf.taxaccounting.model.result.DeleteDeclarationTypeAssignmentsResult;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeAssignmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * Получение списка назначений налоговых форм подразделениям
     *
     * @param filter       Значения фильтра
     * @param pagingParams Параметры для пагинации
     * @return Страница списка назначений налоговых форм подразделениям {@link DeclarationTypeAssignment}
     */
    @GetMapping(value = "/rest/declarationTypeAssignment")
    public JqgridPagedList<DeclarationTypeAssignment> fetchDeclarationTypeAssignments(@RequestParam DeclarationTypeAssignmentFilter filter, @RequestParam PagingParams pagingParams) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        PagingResult<DeclarationTypeAssignment> pagingResult = assignmentService.fetchDeclarationTypeAssignments(userInfo, filter, pagingParams);

        return JqgridPagedResourceAssembler.buildPagedList(
                pagingResult,
                pagingResult.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Создание назначения налоговых форм подраздениям
     *
     * @param action Модель с данными - объект, содержащий подразделения, исполнителей и виды налоговых форм {@link CreateDeclarationTypeAssignmentAction}
     * @return Результат операции {@link CreateDeclarationTypeAssignmentResult} с uuid группы сообщений и информацией о повторном назначении
     */
    @PostMapping(value = "/actions/declarationTypeAssignment/create")
    public CreateDeclarationTypeAssignmentResult createDeclarationTypeAssignment(CreateDeclarationTypeAssignmentAction action) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return assignmentService.createDeclarationTypeAssignment(userInfo, action);
    }

    /**
     * Редактирование назначений налоговых форм подраздениям
     *
     * @param action Модель с данными - объект, содержащий существующие назначения налоговых форм и исполнителей {@link EditDeclarationTypeAssignmentsAction}
     */
    @PostMapping(value = "/actions/declarationTypeAssignment/edit")
    public void editDeclarationTypeAssignment(EditDeclarationTypeAssignmentsAction action) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        assignmentService.editDeclarationTypeAssignments(userInfo, action);
    }

    /**
     * Отмена назначений налоговых форм подразденениям
     *
     * @param assignments Список упрощенных моделей назначений {@link DeclarationTypeAssignmentIdModel}
     * @return Результат выполнения операции {@link DeleteDeclarationTypeAssignmentsResult}
     */
    @PostMapping(value = "/actions/declarationTypeAssignment/delete")
    public DeleteDeclarationTypeAssignmentsResult deleteDeclarationTypeAssignment(@RequestBody List<DeclarationTypeAssignmentIdModel> assignments) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return assignmentService.deleteDeclarationTypeAssignments(userInfo, assignments);
    }
}
