package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationTypeAssignmentAction;
import com.aplana.sbrf.taxaccounting.model.result.CreateDeclarationTypeAssignmentResult;

/**
 * Сервис для работы с назначением налоговых форм {@link DeclarationTypeAssignment}
 */
public interface DeclarationTypeAssignmentService {
    /**
     * Получение страницы списка назначений налоговых форм подразделениям, доступным пользователю
     *
     * @param filter       Параметры фильтрации
     * @param userInfo     Информация о пользователе
     * @param pagingParams Параметры пагинации
     * @return Список назначений налоговых форм подразделениям {@link DeclarationTypeAssignment}
     */
    PagingResult<DeclarationTypeAssignment> fetchDeclarationTypeAssignments(TAUserInfo userInfo, DeclarationTypeAssignmentFilter filter, PagingParams pagingParams);

    /**
     * Создание назначения налоговых форм подраздениям
     *
     * @param userInfo Информация о пользователе
     * @param action   Модель с данными
     * @return Результат назначения {@link CreateDeclarationTypeAssignmentResult}
     */
    CreateDeclarationTypeAssignmentResult createDeclarationTypeAssignment(TAUserInfo userInfo, CreateDeclarationTypeAssignmentAction action);
}
