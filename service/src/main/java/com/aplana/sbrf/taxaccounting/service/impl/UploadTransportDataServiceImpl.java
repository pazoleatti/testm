package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * Сервис для обработки загружаемых файлов
 *
 * @author dloshkarev
 */
@Service
public class UploadTransportDataServiceImpl implements UploadTransportDataService {
    private static final Log LOG = LogFactory.getLog(UploadTransportDataServiceImpl.class);

    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private RefBookScriptingService refBookScriptingService;

    private static final String CREATE_TASK = "Загрузка файла %s%s поставлена в очередь на исполнение";
    private static final String ARCHIVE_INFO = " из архива \"%s\"";
    private static final String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    private static final String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_TAXES_SERVICE)")
    public ActionResult uploadFile(TAUserInfo userInfo, String fileName, InputStream inputStream, long fileSize) {
        LOG.info(String.format("UploadTransportDataServiceImpl.uploadFile. fileName: %s", fileName));
        Logger logger = new Logger();

        if (fileName == null) {
            logger.error(NO_FILE_NAME_ERROR);
            return new ActionResult(logEntryService.save(logger.getEntries()));
        }

        if (inputStream == null) {
            logger.error(EMPTY_INPUT_STREAM_ERROR);
            return new ActionResult(logEntryService.save(logger.getEntries()));
        }

        if ("zip".equals(FilenameUtils.getExtension(fileName))) {
            logger.info("Обработка архива \"%s\"", fileName);
            // Если это архив - распаковываем, сохраняем в блобы и обрабатываем каждый файл отдельно
            try {
                // Такая кодировка нужна для исправления бага IllegalArgumentException: MALFORMED
                ZipInputStream zis = new ZipInputStream(inputStream, Charset.forName("CP866"));
                ZipEntry entry;

                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        if (entry.getSize() == 0) {
                            logger.error("Файл " + entry.getName() + " не должен быть пуст");
                        } else {
                            // Сохраняем в блоб и начинаем его обработку в зависимости от формата
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            IOUtils.copy(zis, baos);
                            String uuid = blobDataService.create(new ByteArrayInputStream(baos.toByteArray()), entry.getName().substring(entry.getName().lastIndexOf("/") + 1));
                            processFileByFormat(userInfo, fileName, entry.getName().substring(entry.getName().lastIndexOf("/") + 1), uuid, fileSize, logger);
                        }
                    }
                }
            } catch (ZipException e) {
                LOG.error("Произошла ошибка при обработке архива", e);
                String msg;
                if ("encrypted ZIP entry not supported".equals(e.getMessage())) {
                    msg = "Ошибка при обработке архива " + fileName + ". На архив установлен пароль.";
                } else {
                    msg = "Ошибка при обработке архива " + fileName + ". Нарушена структура архива.";
                }
                logger.error(msg);
                throw new ServiceLoggerException("Произошла ошибка при обработке архива", logEntryService.save(logger.getEntries()));
            } catch (IOException e) {
                LOG.error("Произошла ошибка при обработке архива", e);
                logger.error(e.getLocalizedMessage());
                throw new ServiceLoggerException("Произошла ошибка при обработке архива", logEntryService.save(logger.getEntries()));
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } else {
            // Если не архив - обрабатываем файл в зависимости от формата
            String uuid = blobDataService.create(inputStream, fileName);
            processFileByFormat(userInfo, null, fileName, uuid, fileSize, logger);
        }

        // Сохраняем и выводим результат обработки всех файлов
        return new ActionResult(logEntryService.save(logger.getEntries()));
    }

    /**
     * Определяет тип ТФ в зависимости от его имени
     *
     * @param fileName имя файла
     * @return тип ТФ
     */
    public static TransportFileType getFileType(String fileName) {
        if (fileName.length() == 63 && "xml".equals(FilenameUtils.getExtension(fileName))) {
            // ТФ РНУ НДФЛ
            return TransportFileType.RNU_NDFL;
        } else if (fileName.startsWith("KV_NONDFL") || fileName.startsWith("UO_NONDFL") ||
                fileName.startsWith("IV_NONDFL") || fileName.startsWith("UU_NONDFL")) {
            // Файл ответа 6-НДФЛ
            return TransportFileType.RESPONSE_6_NDFL;
        } else if (fileName.toLowerCase().startsWith("prot_no_ndfl2") || fileName.toLowerCase().startsWith("прот_no_ndfl2") ||
                fileName.toLowerCase().startsWith("reestr_no_ndfl2") || fileName.toLowerCase().startsWith("реестр_no_ndfl2")) {
            // Файл ответа 2-НДФЛ
            return TransportFileType.RESPONSE_2_NDFL;
        }
        return null;
    }

    /**
     * Маршрутизирует обработку файла в зависимости от его формата (имени файла)
     *
     * @param userInfo     текущий пользователь
     * @param archiveName  название входящего архива с файлами (может быть null)
     * @param fileName     название файла
     * @param fileBlobUuid идентификатор временного блоба, в который сохранен файл
     * @param fileSize     размер файла (байт)
     * @param logger       логгер
     */
    private void processFileByFormat(TAUserInfo userInfo, String archiveName, String fileName, String fileBlobUuid, long fileSize, Logger logger) {
        if (fileName.startsWith("FL") && "xml".equals(FilenameUtils.getExtension(fileName))) {
            // Файл первичной загрузки ФЛ
            createTaskToImportXmlFL(userInfo, archiveName, fileName, fileBlobUuid, fileSize, logger);
        } else {
            // ТФ РНУ НДФЛ, файла ответа 6-НДФЛ, файла ответа 2-НДФЛ
            TransportFileType fileType = getFileType(fileName);
            if (fileType != null) {
                createTaskToImportTF(userInfo, fileType, archiveName, fileName, fileBlobUuid, fileSize, logger);
            } else {
                logger.error("Некорректное имя или формат файла \"%s\"", fileName);
            }
        }
    }

    /**
     * Запускает асинхронную задачу для обработки файла первичной загрузки ФЛ
     *
     * @param userInfo     текущий пользователь
     * @param archiveName  название входящего архива с файлами (может быть null)
     * @param fileName     название файла
     * @param fileBlobUuid идентификатор временного блоба, в который сохранен файл
     * @param fileSize     размер файла (байт)
     * @param logger       логгер
     */
    @PreAuthorize("hasRole('N_ROLE_CONTROL_UNP')")
    private void createTaskToImportXmlFL(TAUserInfo userInfo, String archiveName, String fileName, String fileBlobUuid, long fileSize, Logger logger) {
        LOG.info(String.format("UploadTransportDataServiceImpl.createTaskToImportXmlFL. userInfo: %s; fileName: %s; fileBlobUuid: %s; ",
                userInfo, fileName, fileBlobUuid));
        long refBookId = RefBook.Id.PERSON.getId();

        Configuration isImportEnabledConfiguration = configurationDao.fetchByEnum(ConfigurationParam.ENABLE_IMPORT_PERSON);
        if (isImportEnabledConfiguration != null && "1".equals(isImportEnabledConfiguration.getValue())) {
            final TAUser user = userInfo.getUser();
            RefBook refBook = commonRefBookService.get(refBookId);

            String refBookLockKey = commonRefBookService.generateTaskKey(refBookId);
            LockData refBookLockData = lockDataService.findLock(refBookLockKey);
            if (refBookLockData != null && refBookLockData.getUserId() != user.getId()) {
                logger.error(commonRefBookService.getRefBookLockDescription(refBookLockData, refBook.getId()));
                logger.error("Загрузка файла \"%s\" не может быть выполнена.", fileName);
            } else {
                String asyncLockKey = LockData.LockObjects.IMPORT_REF_BOOK_XML.name() + "_" + refBookId + "_" + fileName;
                LockData asyncLock = lockDataService.lock(asyncLockKey, user.getId(), String.format(DescriptionTemplate.IMPORT_TRANSPORT_DATA_PERSON_XML.getText(), fileName));
                if (asyncLock != null) {
                    logger.error("Не удалось запустить задачу. Возможно загрузка в справочник ФЛ уже выполняется");
                } else {
                    try {
                        Map<String, Object> params = new HashMap<>();
                        params.put("refBookId", refBookId);
                        params.put("blobDataId", fileBlobUuid);
                        params.put("refBookName", refBook.getName());
                        params.put("fileSize", fileSize);
                        if (archiveName != null) {
                            params.put("archiveName", archiveName);
                        }
                        asyncManager.executeTask(asyncLockKey, AsyncTaskType.IMPORT_REF_BOOK_XML, userInfo, params);
                        logger.info(String.format(CREATE_TASK, fileName, archiveName != null ? String.format(ARCHIVE_INFO, archiveName) : ""));
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        } else {
            logger.error("Загрузка файлов справочника ФЛ отключена. Обратитесь к администратору");
        }
    }

    /**
     * Запускает асинхронную задачу для обработки в скрипте: ТФ РНУ НДФЛ, файла ответа 6-НДФЛ, файла ответа 2-НДФЛ
     * //TODO: метод один на 3 типа файлов, потому что и асинхронная задача у нас для этого одна
     *
     * @param userInfo     текущий пользователь
     * @param fileType     тип ТФ
     * @param archiveName  название входящего архива с файлами (может быть null)
     * @param fileName     название файла
     * @param fileBlobUuid идентификатор временного блоба, в который сохранен файл
     * @param fileSize
     * @param logger       логгер
     */
    @PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_NS', 'N_ROLE_CONTROL_UNP')")
    private void createTaskToImportTF(TAUserInfo userInfo, TransportFileType fileType, String archiveName, String fileName, String fileBlobUuid, long fileSize, Logger logger) {
        LOG.info(String.format("UploadTransportDataServiceImpl.createTaskToImportTF. fileName: %s; fileBlobUuid: %s", fileName, fileBlobUuid));
        int userId = userInfo.getUser().getId();
        String key = LockData.LockObjects.LOAD_TRANSPORT_DATA.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        LockData lockData = lockDataService.lock(key, userId, String.format(DescriptionTemplate.IMPORT_TRANSPORT_DATA.getText(), fileName));
        if (lockData == null) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("blobDataId", fileBlobUuid);
                params.put("fileType", fileType);
                params.put("fileSize", fileSize);
                if (archiveName != null) {
                    params.put("archiveName", archiveName);
                }
                try {
                    asyncManager.executeTask(key, AsyncTaskType.LOAD_TRANSPORT_FILE, userInfo, params);
                    logger.info(String.format(CREATE_TASK, fileName, archiveName != null ? String.format(ARCHIVE_INFO, archiveName) : ""));
                } catch (AsyncTaskException e) {
                    lockDataService.unlock(key, userId);
                    logger.error("Ошибка при постановке в очередь задачи загрузки ТФ.");
                }
            } catch (Exception e) {
                logger.error(e);
                try {
                    lockDataService.unlock(key, userId);
                } catch (ServiceException e2) {
                    logger.error(e2);
                }
            }
        }
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_TAXES_SERVICE)")
    public ActionResult uploadAll(TAUserInfo userInfo, Logger logger) {
        LOG.info(String.format("UploadTransportDataServiceImpl.uploadAll. userInfo: %s", userInfo));
        int userId = userInfo.getUser().getId();
        String key = LockData.LockObjects.LOAD_TRANSPORT_DATA.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        LockData lockData = lockDataService.lock(key, userId, DescriptionTemplate.LOAD_TRANSPORT_DATA.getText());
        if (lockData == null) {
            try {
                try {
                    AsyncTaskData taskData = asyncManager.executeTask(key, AsyncTaskType.LOAD_ALL_TRANSPORT_DATA, userInfo);
                    asyncManager.addUserWaitingForTask(taskData.getId(), userId);
                    logger.info("Задача загрузки ТФ запущена");
                } catch (AsyncTaskException e) {
                    lockDataService.unlock(key, userId);
                    logger.error("Ошибка при постановке в очередь задачи загрузки ТФ.");
                }
            } catch (Exception e) {
                try {
                    lockDataService.unlock(key, userId);
                } catch (ServiceException e2) {
                    if (applicationInfo.isProductionMode() || !(e instanceof RuntimeException)) { // в debug-режиме не выводим сообщение об отсутсвии блокировки, если оня снята при выбрасывании исключения
                        throw e2;
                    }
                }
                if (e instanceof ServiceLoggerException) {
                    throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                } else {
                    throw new ServiceException(e.getMessage(), e);
                }
            }
        }

        ActionResult result = new ActionResult();
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public String processTransportFileUploading(Logger logger, TAUserInfo userInfo, TransportFileType fileType, String fileName, InputStream inputStream, long taskId) {
        // Дополнительные проверки, на случай, если блоб криво сохранился в БД
        if (fileName == null) {
            throw new ServiceLoggerException(NO_FILE_NAME_ERROR, logEntryService.save(logger.getEntries()));
        }
        if (inputStream == null) {
            throw new ServiceLoggerException(EMPTY_INPUT_STREAM_ERROR, logEntryService.save(logger.getEntries()));
        }

        File dataFile = null;
        StringBuilder msgBuilder = new StringBuilder();
        LockData fileLock = lockDataService.lock(LockData.LockObjects.FILE.name() + "_" + fileName,
                userInfo.getUser().getId(),
                String.format(DescriptionTemplate.FILE.getText(), fileName));
        if (fileLock != null) {
            logger.error("Файл %s пропущен, т.к. он уже обрабатывается системой", fileName);
        } else {
            OutputStream dataFileOutputStream = null;
            InputStream dataFileInputStream = null;
            try {
                dataFile = File.createTempFile("dataFile", ".original");
                dataFileOutputStream = new FileOutputStream(dataFile);
                try {
                    IOUtils.copy(inputStream, dataFileOutputStream);
                } finally {
                    IOUtils.closeQuietly(dataFileOutputStream);
                }

                dataFileInputStream = new BufferedInputStream(new FileInputStream(dataFile));
                Map<String, Object> additionalParameters = new HashMap<>();
                additionalParameters.put("ImportInputStream", dataFileInputStream);
                additionalParameters.put("UploadFileName", fileName);
                additionalParameters.put("dataFile", dataFile);
                additionalParameters.put("fileType", fileType);
                additionalParameters.put("msgBuilder", msgBuilder);
                refBookScriptingService.executeScript(userInfo, RefBook.Id.DECLARATION_TEMPLATE.getId(), FormDataEvent.IMPORT_TRANSPORT_FILE, logger, additionalParameters);
            } catch (Exception e) {
                LOG.error("Непредвиденная ошибка при обработке файла", e);
                throw new ServiceLoggerException(e.getLocalizedMessage(), logEntryService.save(logger.getEntries()));
            } finally {
                IOUtils.closeQuietly(dataFileOutputStream);
                IOUtils.closeQuietly(dataFileInputStream);
                if (dataFile != null) {
                    dataFile.delete();
                }
                lockDataService.unlock(LockData.LockObjects.FILE.name() + "_" + fileName, userInfo.getUser().getId(), true);
            }
            logger.info("Завершена обработка файла \"%s\"", fileName);
        }
        return msgBuilder.toString();
    }
}
