package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
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
     * @param departmentId Подразделение, в каталог которого пойдет загрузка
     * @param fileName Имя ТФ
     * @param inputStream Поток ТФ
     * @param logger Логгер для области уведомлений
     * @return true — был загружен хотя бы один файл
     */
    boolean uploadFile(TAUserInfo userInfo, int departmentId, String fileName, InputStream inputStream, Logger logger) throws IOException;
}
