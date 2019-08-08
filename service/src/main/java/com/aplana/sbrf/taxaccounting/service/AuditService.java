package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.AuditFormType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.Date;

/**
 * Сервис для работы с журналом аудита
 */
@ScriptExposed
public interface AuditService {

    /**
     * Создание записи в ЖА
     * @param event событие {@link FormDataEvent} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param note пояснение (обязательное)
     */
    void add(FormDataEvent event, TAUserInfo userInfo, String note);

    /**
     * Добавить информацию об логировании
     * @param event событие {@link FormDataEvent} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param departmentId подразделение НФ/декларации (необязательное)
     * @param reportPeriodId отчетный период (необязательное)
     * @param declarationTypeName наименование типа декларации (необязательное)
     * @param formTypeName наименование типа НФ (необязательное) Хранится для информации о виде НФ, даже если она будет изменена
     * @param formKindId вид НФ (необязательное)
     * @param note пояснение (необязательное)
     * @param blobDataId ссылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     */
    void add(FormDataEvent event, TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId,
             String declarationTypeName, String formTypeName, AuditFormType auditFormType, Integer formKindId, String note, String blobDataId);

    /**
     * Добавить информацию о логировании из версий макетов
     * @param event событие {@link FormDataEvent} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param startDate дата начала действия макета
     * @param endDate дата окончания действия макета
     * @param declarationTemplateName наименование типа декларации (необязательное)
     * @param formTemplateName наименование типа НФ (необязательное) Хранится для информации о виде НФ, даже если она будет изменена
     * @param note пояснение (необязательное)
     * @param blobDataId ссылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     */
    void add(FormDataEvent event, TAUserInfo userInfo, Date startDate, Date endDate,
             String declarationTemplateName, String formTemplateName, String note, String blobDataId);

    /**
     * Логгирование для НФ/деклараций(т.к. нужно еще инфо о корр. периоде добавлять)
     * @param event событие {@link FormDataEvent} (необязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param declarationData декларация (обязательное)
     * @param note пояснение (необязательное)
     * @param blobDataId ссылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     */
    void add(FormDataEvent event, TAUserInfo userInfo, DeclarationData declarationData, String note, String blobDataId);
}
