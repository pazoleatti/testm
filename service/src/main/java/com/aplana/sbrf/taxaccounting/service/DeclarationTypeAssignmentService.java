package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

/**
 * Сервис для работы с назначением налоговых форм
 */
public interface DeclarationTypeAssignmentService {
    /**
     * Получение списка назначений налоговых форм подразделениям, доступным пользователю
     *
     * @param userInfo     Информация о пользователе
     * @param pagingParams Параметры пагинации
     * @return Список назначений
     */
    PagingResult<FormTypeKind> fetchDeclarationTypeAssignments(TAUserInfo userInfo, PagingParams pagingParams);
}
