package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lhaziev
 */
@Service
@Transactional
public class LoadDeclarationDataServiceImpl extends AbstractLoadTransportDataService implements LoadDeclarationDataService {

	private static final Log LOG = LogFactory.getLog(LoadDeclarationDataServiceImpl.class);
    private static final String LOCK_MSG = "Обработка данных транспортного файла не выполнена, " +
            "т.к. в данный момент выполняется изменение данных налоговой формы \"%s\" " +
            "для подразделения \"%s\" " +
            "в периоде \"%s\", " +
            "инициированное пользователем \"%s\" " +
            "в %s";
    private static final ThreadLocal<SimpleDateFormat> SDF_HH_MM_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm dd.MM.yyyy");
        }
    };

    // Сообщения, которые не учтены в постановка
    static final String ACCESS_DENIED_ERROR = "У пользователя нет прав для загрузки транспортных файлов!";
    static final String NO_FILE_NAME_ERROR = "Невозможно определить имя файла!";
    static final String EMPTY_INPUT_STREAM_ERROR = "Поток данных пуст!";

    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private TAUserService userService;
    @Autowired
    private RefBookScriptingService refBookScriptingService;

    /**
     * Загрузка ТФ конкретной декларации. Только этот метод в сервисе транзакционный.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String uploadFile(Logger logger, TAUserInfo userInfo, String fileName, InputStream inputStream, String lock) {
        ImportCounter importCounter = uploadFileWithoutLog(userInfo, fileName, inputStream, logger, lock);
        logger.info(LogData.L35.getText(), importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return StringUtils.join(importCounter.getMsgList().toArray(), ", ", null);
    }

    private ImportCounter uploadFileWithoutLog(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger, String lock) {
        if (!userInfo.getUser().hasRoles(TARole.N_ROLE_OPER, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP,
                TARole.F_ROLE_OPER, TARole.F_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_UNP)) {
            logger.error(ACCESS_DENIED_ERROR);
            return new ImportCounter(0, 1);
        }

        if (fileName == null) {
            logger.error(NO_FILE_NAME_ERROR);
            return new ImportCounter(0, 1);
        }

        if (inputStream == null) {
            logger.error(EMPTY_INPUT_STREAM_ERROR);
            return new ImportCounter(0, 1);
        }

        // Счетчики
        int success = 0;
        int fail = 0;
        List<String> msgList = new ArrayList<String>();
        try {
            if (fileName.toLowerCase().endsWith(".zip")) {
                File dataFile = null;
                OutputStream dataFileOutputStream = null;
                try {
                    dataFile = File.createTempFile("zipfile", ".original");
                    dataFileOutputStream = new FileOutputStream(dataFile);
                    IOUtils.copy(inputStream, dataFileOutputStream);
                } catch (IOException e) {
                    log(userInfo, LogData.L33, logger, lock, fileName, e.getMessage());
                    fail++;
                    LOG.error(e.getMessage(), e);
                } finally {
                    IOUtils.closeQuietly(dataFileOutputStream);
                }
                ZipFile zf = new ZipFile(dataFile);
                Enumeration<ZipArchiveEntry> entries = zf.getEntries();
                try {
                    while (entries.hasMoreElements()) {
                        ZipArchiveEntry entry = entries.nextElement();
                        try {
                            InputStream is = zf.getInputStream(entry);
                            Logger localLogger = new Logger();
                            try {
                                if (loadFile(is, entry.getName(), userInfo, localLogger, lock, msgList)) {
                                    success++;
                                } else {
                                    fail++;
                                }
                            } finally {
                                logger.getEntries().addAll(localLogger.getEntries());
                            }
                        } catch (ServiceException se) {
                            log(userInfo, LogData.L33, logger, lock, entry.getName(), se.getMessage());
                            fail++;
                            LOG.error(se.getMessage(), se);
                        }
                    }
                } catch (IOException e) {
                    // Ошибка копирования из архива
                    log(userInfo, LogData.L33, logger, lock, fileName, e.getMessage());
                    fail++;
                    LOG.error(e.getMessage(), e);
                } catch (ServiceException se) {
                    log(userInfo, LogData.L33, logger, lock, fileName, se.getMessage());
                    fail++;
                    LOG.error(se.getMessage(), se);
                }
            } else {
                try {
                    if (loadFile(inputStream, fileName, userInfo, logger, lock, msgList)) {
                        success++;
                    } else {
                        fail++;
                    }
                } catch (IOException e) {
                    // Ошибка копирования файла
                    log(userInfo, LogData.L33, logger, lock, fileName, e.getMessage());
                    fail++;
                    LOG.error(e.getMessage(), e);
                } catch (ServiceException se) {
                    log(userInfo, LogData.L33, logger, lock, fileName, se.getMessage());
                    fail++;
                    LOG.error(se.getMessage(), se);
                }
            }
        } catch (IOException e) {
            log(userInfo, LogData.L33, logger, lock, fileName, e.getMessage());
            fail++;
            LOG.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return new ImportCounter(success, fail, msgList);
    }

    private boolean loadFile(InputStream inputStream, String fileName, TAUserInfo userInfo, Logger logger, String lock, List<String> msgList) throws IOException {
        File dataFile = null;
        LockData fileLock = lockDataService.lock(LockData.LockObjects.FILE.name() + "_" + fileName,
                userInfo.getUser().getId(),
                String.format(LockData.DescriptionTemplate.FILE.getText(), fileName));
        if (fileLock != null) {
            logger.error("Файл %s пропущен, т.к. он уже обрабатывается системой", fileName);
            return false;
        } else {
            try {
                dataFile = File.createTempFile("dataFile", ".original");
                OutputStream dataFileOutputStream = new FileOutputStream(dataFile);
                try {
                    IOUtils.copy(inputStream, dataFileOutputStream);
                } finally {
                    IOUtils.closeQuietly(dataFileOutputStream);
                }
                try {
                    InputStream dataFileInputStream = new BufferedInputStream(new FileInputStream(dataFile));
                    try {
                        Map<String, Object> additionalParameters = new HashMap<String, Object>();
                        StringBuilder msgBuilder = new StringBuilder();
                        additionalParameters.put("ImportInputStream", dataFileInputStream);
                        additionalParameters.put("UploadFileName", fileName);
                        additionalParameters.put("dataFile", dataFile);
                        additionalParameters.put("msgBuilder", msgBuilder);
                        refBookScriptingService.executeScript(userInfo, RefBook.Id.DECLARATION_TEMPLATE.getId(), FormDataEvent.IMPORT_TRANSPORT_FILE, logger, additionalParameters);
                        if (logger.containsLevel(LogLevel.ERROR)) {
                            throw new ServiceException("Есть критические ошибки при выполнении скрипта");
                        }
                        if (msgBuilder.length() > 0) {
                            msgList.add(msgBuilder.toString());
                        }
                    } finally {
                        IOUtils.closeQuietly(dataFileInputStream);
                    }
                } catch (FileNotFoundException e) {
                    throw new ServiceException(e.getLocalizedMessage(), e);
                }
            } finally {
                if (dataFile != null) {
                    dataFile.delete();
                }
                lockDataService.unlock(LockData.LockObjects.FILE.name() + "_" + fileName, userInfo.getUser().getId(), true);
            }
            logger.info("Завершена обработка файла \"%s\"", fileName);
            return true;
        }
    }

    @Override
    public void importDeclarationData(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, InputStream inputStream,
                                      String fileName, File dataFile, AttachFileType attachFileType, LocalDateTime createDateFile) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declarationData.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationType declarationType = declarationTemplate.getType();
        String reportPeriodName = departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + " - "
                + departmentReportPeriod.getReportPeriod().getName();

        // Блокировка
        LockData lockData = lockDataService.lock(declarationDataService.generateAsyncTaskKey(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC),
                userInfo.getUser().getId(),
                declarationDataService.getDeclarationFullName(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC));
        if (lockData != null)
            throw new ServiceException(String.format(
                    LOCK_MSG,
                    declarationType.getName(),
                    departmentService.getDepartment(declarationData.getDepartmentId()).getName(),
                    reportPeriodName,
                    userService.getUser(lockData.getUserId()).getName(),
                    SDF_HH_MM_DD_MM_YYYY.get().format(lockData.getDateLock())
            ));

        try {
            // Скрипт загрузки ТФ + прикладываем файл к НФ
            try {
                declarationDataService.importDeclarationData(logger, userInfo, declarationData.getId(), inputStream,
                        fileName, FormDataEvent.IMPORT_TRANSPORT_FILE, null, dataFile, attachFileType, createDateFile);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } finally {
            // Снимаем блокировку
            lockDataService.unlock(declarationDataService.generateAsyncTaskKey(declarationData.getId(), DeclarationDataReportType.IMPORT_TF_DEC), userInfo.getUser().getId());
        }
    }
}
