package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.UploadResult;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import java.io.IOException;
import java.io.InputStream;

/**
 * Сервис загрузки ТФ в каталог загрузки
 *
 * @author Dmitriy Levykin
 */
public interface UploadTransportDataService {
    /**
     * Загрузка ТФ в каталог загрузки. Загружаются ТФ НФ и ТФС.
     *
     * @param userInfo    Пользователь
     * @param fileName    Имя ТФ
     * @param inputStream Поток ТФ
     * @param logger      Логгер для области уведомлений
     * @return true — был загружен хотя бы один файл
     */
    UploadResult uploadFile(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger) throws IOException;

    /**
     * Загрузка выбранного пользователем ТФ
     *
     * @param userInfo    Пользователь
     * @param fileName    Имя ТФ
     * @param inputStream Поток ТФ
     * @param logger      Логгер для области уведомлений
     * @return ActionResult результат выполнения операции с UUID группы сообщений в журнале
     */
    ActionResult upload(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger);

    /**
     * Загрузка всех файлов из каталога загрузки
     *
     * @param userInfo Пользователь
     * @param logger   Логгер для области уведомлений
     * @return ActionResult результат выполнения операции с UUID группы сообщений в журнале
     */
    ActionResult uploadAll(TAUserInfo userInfo, Logger logger);


}
