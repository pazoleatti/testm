package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.UploadResult;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

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
     * @param userInfo Пользователь
     * @param fileName Имя ТФ
     * @param inputStream Поток ТФ
     * @param logger Логгер для области уведомлений
     * @return true — был загружен хотя бы один файл
     */
    UploadResult uploadFile(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger) throws IOException;
}
