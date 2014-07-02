package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Сервис загрузки транспортных файлов
 */
public interface TransportDataService {
    /**
     * Загрузка ТФ в каталог загрузки
     * @param userInfo Пользователь
     * @param departmentId Подразделение, в каталог которого пойдет загрузка
     * @param fileName Имя ТФ
     * @param inputStream Поток ТФ
     * @param logger Логгер для области уведомлений
     * @throws IOException
     */
    void uploadFile(TAUserInfo userInfo, int departmentId, String fileName, InputStream inputStream, Logger logger) throws IOException;

    /**
     * Импорт ТФ из каталога загрузки (Планировщик)
     * @param userInfo Пользователь
     * @param departmentList Список подразделений, для которых нужно выполнить импорт
     * @param folderParam Ключ конф. параметра, по которому ищется каталог
     * @param logger Логгер для области уведомлений
     */
    void importDataFromFolder(TAUserInfo userInfo, List<Department> departmentList, ConfigurationParam folderParam, Logger logger);

    /**
     * Импорт ТФ из каталога загрузки (Ручная загрузка)
     * @param userInfo Пользователь
     * @param folderParam Ключ конф. параметра, по которому ищется каталог
     * @param logger Логгер для области уведомлений
     */
    void importDataFromFolder(TAUserInfo userInfo, ConfigurationParam folderParam, Logger logger);

    /**
     * Получение спика файлов из каталога загрузки. Некоторые файлы могут быть пропущены.
     */
    List<String> getWorkFilesFromFolder(String folderPath);
}
