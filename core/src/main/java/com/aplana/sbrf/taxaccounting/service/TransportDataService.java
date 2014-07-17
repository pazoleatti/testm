package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Сервис загрузки транспортных файлов
 *
 * @author Dmitriy Levykin
 */
public interface TransportDataService {
    /**
     * Загрузка ТФ в каталог загрузки
     * @param userInfo Пользователь
     * @param departmentId Подразделение, в каталог которого пойдет загрузка
     * @param fileName Имя ТФ
     * @param inputStream Поток ТФ
     * @param logger Логгер для области уведомлений
     * @return true — был загружен хотя бы один файл
     */
    boolean uploadFile(TAUserInfo userInfo, int departmentId, String fileName, InputStream inputStream, Logger logger) throws IOException;

    /**
     * Импорт ТФ из одного каталога загрузки
     * @param userInfo Пользователь
     * @param folderParam Ключ конф. параметра, по которому ищется каталог
     * @param departmentId Подразделение. Для общих параметров не используется
     * @param logger Логгер для области уведомлений
     */
    ImportCounter importDataFromFolder(TAUserInfo userInfo, ConfigurationParam folderParam, Integer departmentId, Logger logger);

    /**
     * Получение спика ТФ НФ из каталога загрузки. Файлы, которые не соответствуют маппингу пропускаются.
     */
    List<String> getWorkFilesFromFolder(String folderPath, Set<String> ignoreFileSet);
}
