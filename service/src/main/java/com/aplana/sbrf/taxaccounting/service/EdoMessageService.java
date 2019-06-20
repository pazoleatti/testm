package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.impl.transport.edo.SendToEdoResult;

import java.util.List;

/**
 * Сервис для отправки сообщений в ЭДО
 */
public interface EdoMessageService {

    /**
     * Отправляет данные ОНФ в ЭДО
     *  @param declarationDataIds список ид форм
     * @param userInfo           пользователь запустивший операцию
     * @param logger             логгер
     */
    SendToEdoResult sendToEdo(List<Long> declarationDataIds, TAUserInfo userInfo, Logger logger);
}
