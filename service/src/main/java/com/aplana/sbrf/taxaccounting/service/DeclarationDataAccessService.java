package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Set;

/**
 * Интерфейс для проверки прав пользователя на работу с {@link com.aplana.sbrf.taxaccounting.model.DeclarationData декларациями}
 *
 * @author dsultanbekov
 * @deprecated проверки прав делать через {@link PreAuthorize}
 */
@Deprecated
public interface DeclarationDataAccessService {
    /**
     * Проверяет возможность выполнения действия пользователем, над существующей декларацией.
     * Метод генерит AccessDeniedException если есть проблемы с выполнение действия
     *
     * @param userInfo          - информация о пользователе
     * @param declarationDataId - id декларации
     * @param scriptEvent       - событие (действие)
     * @deprecated проверки прав делать через {@link PreAuthorize}
     */
    @Deprecated
    void checkEvents(TAUserInfo userInfo, Long declarationDataId, FormDataEvent scriptEvent);

    /**
     * Проверяет возможность выполнения действия пользователем, над ещё не существующей декларацией
     * (Теоретически это может быть только создание декларации)
     * Метод генерит AccessDeniedException если есть проблемы с выполнением действия и логгер не задан,
     * если логгер задан, то ошибка записывается в лог
     *
     * @param userInfo               - информация о пользователе
     * @param declarationTemplateId  - id шаблона декларации
     * @param departmentReportPeriod Отчетный период подразделения
     * @param asnuId                 id АСНУ ТФ
     * @param scriptEvent            - событие (действие)
     * @param logger                 логгер (может быть null, тогда будет выбрасываться исключение)
     * @deprecated проверки прав делать через {@link PreAuthorize}
     */
    @Deprecated
    void checkEvents(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod, Long asnuId,
                     FormDataEvent scriptEvent, Logger logger);

    /**
     * Получить все разрешенные действия над существующим объектом
     *
     * @deprecated проверки прав делать через {@link PreAuthorize}
     */
    @Deprecated
    Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo, Long declarationDataId);

    /**
     * Получить все разрешенные действия над не существующим объектом
     *
     * @deprecated проверки прав делать через {@link PreAuthorize}
     */
    @Deprecated
    Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo, int declarationTemplateId,
                                          DepartmentReportPeriod departmentReportPeriod);
}
