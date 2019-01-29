package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TransportFileType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import java.io.IOException;
import java.io.InputStream;

/**
 * Сервис для работы с загружаемыми файлами
 *
 * @author Dmitriy Levykin
 */
public interface UploadTransportDataService {
    /**
     * Загрузка файла через раздел Сервис -> Загрузить файлы -> Загрузить файл. Форматы файлов могут быть:
     * zip - тогда он распаковывается, а все файлы обрабатываются по другим форматам ниже
     * xml - может быть нескольких типов:
     * Начинается с "FL" - файл первичной загрузки ФЛ
     * Длина полного имени (включая расширение) = 63 - файл ТФ РНУ НДФЛ
     * Имя файла начинается с одного из "KV_NONDFL", "UO_NONDFL", "IV_NONDFL", "UU_NONDFL" - файл ответа 6-НДФЛ
     * Имя файла начинается с одного из "PROT_NO_NDFL2", "прот_NO_NDFL2", "REESTR_NO_NDFL2", "реестр_NO_NDFL2" - файл ответа 2-НДФЛ
     * <p>
     * Запускает выполнение асинхронных задач по каждому файлу
     *
     * @param userInfo    пользователь
     * @param fileName    имя файла
     * @param inputStream содержимое файла
     * @param fileSize    размер файла (байт)
     */
    ActionResult uploadFile(TAUserInfo userInfo, String fileName, InputStream inputStream, long fileSize) throws IOException;

    /**
     * Загрузка всех файлов из каталога загрузки
     *
     * @param userInfo пользователь
     * @param logger   логгер для области уведомлений
     * @return ActionResult результат выполнения операции с UUID группы сообщений в журнале
     */
    ActionResult uploadAll(TAUserInfo userInfo, Logger logger);

    /**
     * Выполняет непосредственную обработку загружаемого ТФ
     *
     * @param logger      логгер для области уведомлений
     * @param userInfo    пользователь
     * @param fileType    тип ТФ
     * @param fileName    имя файла
     * @param inputStream содержимое файла
     * @param taskId      идентификатор асинхронной задачи, которая занимается обработкой файла (необходим для логирования)
     * @return сообщение о результате выполнения загрузки файла, которое используеся в оповещениях
     */
    String processTransportFileUploading(Logger logger, TAUserInfo userInfo, TransportFileType fileType, String fileName, InputStream inputStream, long taskId);
}
