package com.aplana.sbrf.taxaccounting.common.service;

import com.aplana.sbrf.taxaccounting.common.model.EventType;
import com.aplana.sbrf.taxaccounting.common.model.UserInfo;

/**
 * Сервис предоставляющий доступ к API учета налогов для других проектов
 * Конкретная реализация представляет из себя ejb, которая получается через lookup по jndi-имени.
 * Это исключает необходимость указания модуля core как зависимости, а также позволяет использовать разные реализации
 * Пример lookup в контексте Spring. Можно также использовать Remote интерфейс
 * <jee:local-slsb id="commonService" jndi-name="ejblocal:{имя приложения, развернутое на сервере приложений}/common-core.jar/CommonServiceBean#com.aplana.sbrf.taxaccounting.common.service.CommonServiceLocal" business-interface="com.aplana.sbrf.taxaccounting.common.service.CommonService" lookup-home-on-startup="true"/>
 * Далее этот сервис можно использовать как спринговый бин. Для dev-мода, эту строку надо убрать, но при этом в контексте Spring должен существовать какой то бин-заглушка этого интерфейса
 * @author dloshkarev
 */
public interface EventAuditService {

    /**
     * Добавить информацию об логировании в журнал аудита
     * @param event событие {@link com.aplana.sbrf.taxaccounting.common.model.EventType} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param note пояснение (необязательное)
     */
    void addAuditLog(EventType event, UserInfo userInfo, String note) throws CommonServiceException;

    /**
     * Добавить информацию об логировании в журнал аудита
     * @param event событие {@link com.aplana.sbrf.taxaccounting.common.model.EventType} (обязательное)
     * @param userInfo информация о пользователе, который совершает событие (обязательное)
     * @param departmentId подразделение НФ/декларации (необязательное)
     * @param reportPeriodId отчетный период (необязательное)
     * @param declarationType наименование типа декларации (необязательное)
     * @param formType наименование типа НФ (необязательное)
     * @param formKindId вид НФ (необязательное)
     * @param note пояснение (необязательное)
     * @param blobDataId сыылка на сериализованные данные из лог панели, хранящие набор сообщений к данному событию (необязательное)
     */
    void addAuditLog(EventType event, UserInfo userInfo, Integer departmentId, Integer reportPeriodId,
                     String declarationType, String formType, Integer formKindId, String note, String blobDataId, Integer formTypeId) throws CommonServiceException;
}
