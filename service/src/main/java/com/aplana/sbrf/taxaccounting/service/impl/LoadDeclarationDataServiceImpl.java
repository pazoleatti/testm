package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
    public void uploadFile(Logger logger, TAUserInfo userInfo, String fileName, InputStream inputStream, String lock) {
        ImportCounter importCounter = uploadFileWithoutLog(userInfo, fileName, inputStream, logger, lock);
        logger.info(LogData.L35.getText(), importCounter.getSuccessCounter(), importCounter.getFailCounter());
    }

    private ImportCounter uploadFileWithoutLog(TAUserInfo userInfo, String fileName, InputStream inputStream, Logger logger, String lock) {
        if (!userInfo.getUser().hasRole(TARole.ROLE_OPER)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)
                && !userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
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
                        try {
                            ZipArchiveEntry entry = entries.nextElement();
                            InputStream is = zf.getInputStream(entry);
                            logger = new Logger();
                            if (loadFile(is, entry.getName(), userInfo, logger, lock)) {
                                success++;
                            } else {
                                fail++;
                            }
                        } catch (ServiceException se) {
                            log(userInfo, LogData.L33, logger, lock, fileName, se.getMessage());
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
                    if (loadFile(inputStream, fileName, userInfo, logger, lock)) {
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
        return new ImportCounter(success, fail);
    }

    private boolean loadFile(InputStream inputStream, String fileName, TAUserInfo userInfo, Logger logger, String lock) throws IOException {
        File dataFile = null;
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
                    additionalParameters.put("ImportInputStream", dataFileInputStream);
                    additionalParameters.put("UploadFileName", fileName);
                    additionalParameters.put("dataFile", dataFile);
                    refBookScriptingService.executeScript(userInfo, RefBook.Id.DECLARATION_TEMPLATE.getId(), FormDataEvent.IMPORT_TRANSPORT_FILE, logger, additionalParameters);
                    if (logger.containsLevel(LogLevel.ERROR)) {
                        throw new ServiceException("Есть критические ошибки при выполнении скрипта");
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
        }
        logger.info("Завершена обработка файл \"%s\"", fileName);
        return true;
    }

    @Override
    public void importDeclarationData(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, InputStream inputStream,
                                      String fileName, File dataFile, AttachFileType attachFileType, Date createDateFile) {
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
