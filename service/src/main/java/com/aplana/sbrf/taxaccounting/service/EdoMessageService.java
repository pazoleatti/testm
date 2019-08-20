package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.impl.transport.edo.SendToEdoResult;

import java.util.List;

/**
 * Сервис сообщений c ЭДО
 */
public interface EdoMessageService {

    /**
     * Отправляет данные ОНФ в ЭДО
     *  @param declarationDataIds список ид форм
     * @param userInfo           пользователь запустивший операцию
     * @param logger             логгер
     */
    SendToEdoResult sendToEdo(List<Long> declarationDataIds, TAUserInfo userInfo, Logger logger);

    /**
     * Принимает сообщения от ЭДО
     * @param message XML-сообщение, полученное от ЭДО
     */
    void accept(String message);
}
