package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.SignService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Arrays.asList;

/**
 * @author Dmitriy Levykin
 */
@Service
public class LoadRefBookDataServiceImpl extends AbstractLoadTransportDataService implements LoadRefBookDataService {

	private static final Log LOG = LogFactory.getLog(LoadRefBookDataServiceImpl.class);
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private SignService signService;
    @Autowired
    private LockDataService lockService;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;

    // ЦАС НСИ
    private static final long REF_BOOK_OKATO = 3L; // Коды ОКАТО
    private static final long REF_BOOK_RF_SUBJ_CODE = 4L; // Коды субъектов Российской Федерации
    private static final long REF_BOOK_ACCOUNT_PLAN = 101L; // План счетов
    // Diasoft Custody
    private static final long REF_BOOK_EMITENT = 100L; // Эмитенты
    private static final long REF_BOOK_BOND = 84L; // Ценные бумаги

    private static final long REF_BOOK_AVG_COST = 208L; // Средняя стоимость транспортных средств

    private static final String OKATO_NAME = "справочника ОКАТО";
    private static final String REGION_NAME = "справочника «Субъекты РФ»";
    private static final String ACCOUNT_PLAN_NAME = "справочника «План счетов»";
    private static final String DIASOFT_NAME = "справочников Diasoft";
    private static final String NSI_NAME = "справочников ЦАС НСИ";
    private static final String AVG_COST_NAME = "справочника «Средняя стоимость транспортных средств»";
    private static final String LOCK_MESSAGE = "Справочник «%s» заблокирован, попробуйте выполнить операцию позже!";

    //// Справочники ЦАС НСИ
    // ОКАТО
    private static final Map<String, List<Pair<Boolean, Long>>> nsiOkatoMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();
    // Субъекты РФ
    private static final Map<String, List<Pair<Boolean, Long>>> nsiRegionMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();
    // План счетов
    private static final Map<String, List<Pair<Boolean, Long>>> nsiAccountPlanMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();

    //// Справочники АС Diasoft Custody
    // Ценные бумаги и Эмитенты
    private static final Map<String, List<Pair<Boolean, Long>>> diasoftMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();

    // Справочник "Средняя стоимость транспортных средств"
    private static final Map<String, List<Pair<Boolean, Long>>> avgCostMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();

    // Сообщения, которые не учтены в постановка
    private static final String IMPORT_REF_BOOK_ERROR = "Ошибка при загрузке транспортных файлов %s. %s";

    static {
        // TODO Левыкин: Каждый конкретный справочник будет загружаться только архивом или только простым файлом, не обоими способами

        // Ценные бумаги + Эмитенты
        diasoftMappingMap.put("ds\\d{6}\\.nsi", asList(new Pair<Boolean, Long>(true, REF_BOOK_EMITENT),
                new Pair<Boolean, Long>(true, REF_BOOK_BOND)));

        // Средняя стоимость транспортных средств
        avgCostMappingMap.put(".*\\.xlsx", asList(new Pair<Boolean, Long>(true, REF_BOOK_AVG_COST)));

        // Архив "Коды ОКАТО"
        nsiOkatoMappingMap.put("oka.{5}\\..{2}", asList(new Pair<Boolean, Long>(false, REF_BOOK_OKATO)));
        // "Коды ОКАТО"
        nsiOkatoMappingMap.put("payments\\.okato\\..{4}\\..{3}\\..{2}", asList(new Pair<Boolean, Long>(true, REF_BOOK_OKATO)));

        // Архив "Коды субъектов Российской Федерации" (Регионы)
        nsiRegionMappingMap.put("rnu.{5}\\..{2}", asList(new Pair<Boolean, Long>(false, REF_BOOK_RF_SUBJ_CODE)));
        // "Коды субъектов Российской Федерации" (Регионы)
        nsiRegionMappingMap.put("generaluse\\.as_rnu\\..{3}\\..{2}", asList(new Pair<Boolean, Long>(true, REF_BOOK_RF_SUBJ_CODE)));

        // Архив «План счетов»
        nsiAccountPlanMappingMap.put("buh.{5}\\..{2}", asList(new Pair<Boolean, Long>(false, REF_BOOK_ACCOUNT_PLAN)));
        // Файл «План счетов»
        nsiAccountPlanMappingMap.put("bookkeeping\\.bookkeeping\\..{3}\\..{2}", asList(new Pair<Boolean, Long>(true, REF_BOOK_ACCOUNT_PLAN)));
    }

    /**
     * Импорт справочников из каталога
     *
     * @param userInfo              Пользователь
     * @param logger                Логгер
     * @param refBookDirectoryParam Путь к директории
     * @param mappingMap            Маппинг имен: Регулярка → Пара(Признак архива, Id справочника)
     * @param refBookName           Имя справочника для сообщения об ошибке
     * @param move                  Признак необходимости перемещения файла после импорта
     * @param loadedFileNameList    Список файлов, если необходимо загружать определенные файлы
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    private ImportCounter importRefBook(TAUserInfo userInfo, Logger logger, ConfigurationParam refBookDirectoryParam,
                                        Map<String, List<Pair<Boolean, Long>>> mappingMap, String refBookName, boolean move,
                                        List<String> loadedFileNameList, String lockId, boolean isAsync) {
        // Получение пути к каталогу загрузки ТФ
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> refBookDirectoryList = model.get(refBookDirectoryParam, 0);
        if (refBookDirectoryList == null || refBookDirectoryList.isEmpty()) {
            log(userInfo, LogData.L37, logger, lockId, refBookName);
            return new ImportCounter();
        }

        // Счетчики
        int success = 0;
        int fail = 0;

        long maxFileSize = 0;
        if (isAsync) {
            maxFileSize = asyncTaskTypeDao.get(ReportType.LOAD_ALL_TF.getAsyncTaskTypeId(true)).getTaskLimit();
        }

        ImportCounter wrongImportCounter = new ImportCounter();
        // Каталогов может быть несколько, хоть сейчас в ConfigurationParam и ограничено одним значением для всех справочников
        for (String path : refBookDirectoryList) {
            if (!checkPath(path)) {
                log(userInfo, LogData.L42_2, logger, lockId, path, refBookName);
                continue;
            }
            // Набор файлов, которые уже обработали
            Set<String> ignoreFileSet = new HashSet<String>();
            // Файлы которые надо перенести в каталог ошибок
            List<String> errorFileList = new ArrayList<String>();
            // Если изначально нет подходящих файлов то выдаем отдельную ошибку
            List<String> workFilesList = getWorkTransportFiles(userInfo, path, ignoreFileSet, mappingMap.keySet(),
                    loadedFileNameList, errorFileList, logger, wrongImportCounter, lockId);

            if (workFilesList.isEmpty()) {
                log(userInfo, LogData.L31, logger, lockId, refBookName);
            }

            if (move) {
                for (String fileName : errorFileList) {
                    FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);
                    if (currentFile.isFile()){
                        moveToErrorDirectory(userInfo, getRefBookErrorPath(userInfo, logger, lockId), currentFile, null, logger, lockId);
                    }
                }
            }

            if (workFilesList.isEmpty()) {
                return wrongImportCounter;
            }
            // Обработка всех подходящих файлов, с получением списка на каждой итерации
            for (String fileName : workFilesList) {
                ignoreFileSet.add(fileName);
                FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);

                if (maxFileSize != 0 && currentFile.length() / 1024 > maxFileSize) {
                    log(userInfo, LogData.L47, logger, lockId, fileName, currentFile.length() / 1024, path, maxFileSize);
                    if (move) {
                        moveToErrorDirectory(userInfo, getRefBookErrorPath(userInfo, logger, lockId), currentFile, null, logger, lockId);
                    }
                    fail++;
                    continue;
                }
                // Блокировка файла
                LockData fileLock = lockService.lock(LockData.LockObjects.FILE.name() + "_" + fileName,
                        userInfo.getUser().getId(),
                        String.format(LockData.DescriptionTemplate.FILE.getText(), fileName));
                if (fileLock != null) {
                    log(userInfo, LogData.L41, logger, lockId, fileName);
                    fail++;
                    continue;
                }

                try {
                    // ЭЦП
                    List<String> signList = configurationDao.getByDepartment(0).get(ConfigurationParam.SIGN_CHECK, 0);
                    if (signList != null && !signList.isEmpty() && SignService.SIGN_CHECK.equals(signList.get(0))) {
                        boolean check = false;
                        try {
                            check = signService.checkSign(currentFile.getPath(), 0);
                        } catch (Exception e) {
                            log(userInfo, LogData.L36, logger, lockId, e.getMessage());
                        }
                        if (!check) {
                            log(userInfo, LogData.L16, logger, lockId, fileName);
                            fail++;
                            if (move) {
                                moveToErrorDirectory(userInfo, getRefBookErrorPath(userInfo, logger, lockId), currentFile, null, logger, lockId);
                            }
                            log(userInfo, LogData.L20, logger, lockId, currentFile.getName());
                            continue;
                        }
                        log(userInfo, LogData.L15, logger, lockId, fileName);
                    } else {
                        log(userInfo, LogData.L15_1, logger, lockId, fileName);
                    }

                    // Один файл может соответствоваь нескольким справочникам
                    List<Pair<Boolean, Long>> matchList = mappingMap.get(mappingMatch(currentFile.getName(), mappingMap.keySet()));

                    // Локальные логгеры
                    List<Logger> localLoggerList = new ArrayList<Logger>(matchList.size());

                    int skip = 0;
                    boolean load = false;
                    // Попытка загрузить один файл в несколько справочников
                    for (int i = 0; i < matchList.size(); i++) {
                        if (load) {
                            break;
                        }
                        Pair<Boolean, Long> refBookMapPair = matchList.get(i);
                        InputStream is = null;
                        Long refBookId = refBookMapPair.getSecond();
                        localLoggerList.add(new Logger());
                        ScriptStatusHolder scriptStatusHolder = new ScriptStatusHolder();
                        try {
                            is = new BufferedInputStream(currentFile.getInputStream());
                            if (!refBookMapPair.getFirst()) {  // Если это не сам файл, а архив
                                ZipInputStream zis = new ZipInputStream(is);
                                ZipEntry zipFileName = zis.getNextEntry();
                                if (zipFileName != null) { // в архиве есть файл
                                    // дальше работаем с первым файлом архива вместо самого архива
                                    is = zis;
                                }
                            }
                            // Обращение к скрипту
                            Map<String, Object> additionalParameters = new HashMap<String, Object>();
                            additionalParameters.put("inputStream", is);
                            additionalParameters.put("fileName", fileName);
                            additionalParameters.put("scriptStatusHolder", scriptStatusHolder);

                            //Устанавливаем блокировку на справочник
                            List<String> lockedObjects = new ArrayList<String>();
                            String lockKey = LockData.LockObjects.REF_BOOK.name() + "_" + refBookId;
                            int userId = userInfo.getUser().getId();

                            RefBook refBook = refBookDao.get(refBookId);
                            LockData lockData = lockService.lock(lockKey, userId,
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName()));
                            if (lockData == null) {
                                try {
                                    //Блокировка установлена
                                    lockedObjects.add(lockKey);
                                    //Блокируем связанные справочники
                                    List<RefBookAttribute> attributes = refBook.getAttributes();
                                    for (RefBookAttribute attribute : attributes) {
                                        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                                            RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                                            String referenceLockKey = LockData.LockObjects.REF_BOOK.name() + "_" + attribute.getRefBookId();
                                            if (!lockedObjects.contains(referenceLockKey)) {
                                                LockData referenceLockData = lockService.lock(referenceLockKey, userId,
                                                        String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                                                if (referenceLockData == null) {
                                                    //Блокировка установлена
                                                    lockedObjects.add(referenceLockKey);
                                                } else {
                                                    throw new ServiceException(String.format(LOCK_MESSAGE, attributeRefBook.getName()));
                                                }
                                            }
                                        }
                                    }

                                    //Выполняем логику скрипта
                                    refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT_TRANSPORT_FILE,
                                            localLoggerList.get(i), additionalParameters);
                                    IOUtils.closeQuietly(is);
                                    // Обработка результата выполнения скрипта
                                    switch (scriptStatusHolder.getScriptStatus()) {
                                        case SUCCESS:
                                            // Уже загрузили, больше не пытаемся
                                            load = true;
                                            if (move) {
                                                // Перемещение в каталог архива
                                                int positionLogger = logger.getEntries().size();
                                                boolean result = moveToArchiveDirectory(userInfo, getRefBookArchivePath(userInfo,
                                                        logger, lockId), currentFile, logger, lockId);
                                                if (result) {
                                                    success++;
                                                    logger.getEntries().addAll(positionLogger, localLoggerList.get(i).getEntries());
                                                    log(userInfo, LogData.L20, logger, lockId, currentFile.getName());
                                                } else {
                                                    fail++;
                                                    // Если в архив не удалось перенести, то пытаемся перенести в каталог ошибок
                                                    moveToErrorDirectory(userInfo, getRefBookErrorPath(userInfo, logger, lockId), currentFile,
                                                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L12.getText(), lockId, ""))), logger, lockId);
                                                }
                                            } else {
                                                success++;
                                                logger.getEntries().addAll(localLoggerList.get(i).getEntries());
                                                log(userInfo, LogData.L20, logger, lockId, currentFile.getName());
                                            }
                                            break;
                                        case SKIP:
                                            skip++;
                                            localLoggerList.get(i).error("Импорт данных справочника \"%s\" из файла \"%s\".", refBook.getName(), fileName);
                                            localLoggerList.get(i).error(scriptStatusHolder.getStatusMessage());
                                            break;
                                    }
                                } finally {
                                    for (String lock : lockedObjects) {
                                        lockService.unlock(lock, userId);
                                    }
                                }
                            } else {
                                throw new ServiceException(String.format(LOCK_MESSAGE, refBook.getName()));
                            }
                        } catch (Exception e) {
                            // При ошибке второй раз не пытаемся загрузить
                            load = true;
                            IOUtils.closeQuietly(is);
                            fail++;
                            // Ошибка импорта отдельного справочника — откатываются изменения только по нему, импорт продолжается
                            log(userInfo, LogData.L21, logger, lockId, e.getMessage());
                            // Перемещение в каталог ошибок
                            logger.getEntries().addAll(localLoggerList.get(i).getEntries());
                            if (move) {
                                moveToErrorDirectory(userInfo, getRefBookErrorPath(userInfo, logger, lockId), currentFile,
                                        getEntries(localLoggerList), logger, lockId);
                            }
                        }
                    }
                    if (skip == matchList.size()) {
                        // В случае неуспешного импорта в общий лог попадает вывод всех скриптов
                        logger.getEntries().addAll(getEntries(localLoggerList));
                        // Файл пропущен всеми справочниками — неправильный формат
                        log(userInfo, LogData.L38, logger, lockId, refBookName);
                        if (move) {
                            moveToErrorDirectory(userInfo, getRefBookErrorPath(userInfo, logger, lockId), currentFile,
                                    getEntries(localLoggerList), logger, lockId);
                        }
                        fail++;
                    }
                } finally {
                    //Снимаем блокировки
                    lockService.unlock(LockData.LockObjects.FILE.name() + "_" + fileName, userInfo.getUser().getId());
                }
            }
        }
        return new ImportCounter(success, fail + wrongImportCounter.getFailCounter());
    }

    /**
     * Импорт справочников из каталога
     *
     * @param userInfo              Пользователь
     * @param logger                Логгер
     * @param refBookDirectoryParam Путь к директории
     * @param mappingMap            Маппинг имен: Регулярка → Пара(Признак архива, Id справочника)
     * @param loadedFileNameList    Список файлов, если необходимо загружать определенные файлы
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    private void getRefBookFiles(List<TransportFileInfo> fileList, TAUserInfo userInfo, Logger logger, ConfigurationParam refBookDirectoryParam,
                                        Map<String, List<Pair<Boolean, Long>>> mappingMap,
                                        List<String> loadedFileNameList, String lockId) {
        // Получение пути к каталогу загрузки ТФ
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> refBookDirectoryList = model.get(refBookDirectoryParam, 0);
        if (refBookDirectoryList == null || refBookDirectoryList.isEmpty()) {
            return;
        }

        // Каталогов может быть несколько, хоть сейчас в ConfigurationParam и ограничено одним значением для всех справочников
        for (String path : refBookDirectoryList) {
            if (!checkPath(path)) {
                continue;
            }
            // Если изначально нет подходящих файлов то выдаем отдельную ошибку
            List<String> workFilesList = getWorkTransportFiles(userInfo, path, new HashSet<String>(), mappingMap.keySet(),
                    loadedFileNameList, new ArrayList<String>(), logger, new ImportCounter(), lockId);

            if (workFilesList.isEmpty()) {
                return;
            }
            // Обработка всех подходящих файлов, с получением списка на каждой итерации
            for (String fileName : workFilesList) {
                FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);
                fileList.add(new TransportFileInfo(currentFile.getName(), path, currentFile.length() / 1024));
            }
        }
        return;
    }

    /**
     * Список сущностей из объединенных логов
     */
    private List<LogEntry> getEntries(List<Logger> loggerList) {
        List<LogEntry> retVal = new LinkedList<LogEntry>();
        for (Logger logger : loggerList) {
            retVal.addAll(logger.getEntries());
        }
        return retVal;
    }

    @Override
    public ImportCounter importRefBookNsi(TAUserInfo userInfo, Logger logger, String lock, boolean isAsync) {
        return importRefBookNsi(userInfo, null, logger, lock, isAsync);
    }

    @Override
    public boolean checkPathArchiveError(TAUserInfo userInfo, Logger logger, String lockId) {
        String archivePath = getRefBookArchivePath(userInfo, logger, lockId);
        String errorPath = getRefBookErrorPath(userInfo, logger, lockId);
        List<String> pathList = new ArrayList<String>();
        if (archivePath == null) {
            pathList.add("к каталогу архива");
        }
        if (errorPath == null) {
            pathList.add("к каталогу ошибок");
        }
        if (!pathList.isEmpty()) {
            if (lockId != null && !lockId.isEmpty()) log(userInfo, LogData.L43, logger, lockId, StringUtils.join(pathList, ", "));
            return false;
        }
        if (!checkPath(archivePath)) {
            pathList.add("к каталогу архива «" + archivePath + "»");
        }
        if (!checkPath(errorPath)) {
            pathList.add("к каталогу ошибок «" + errorPath + "»");
        }
        if (!pathList.isEmpty()) {
            if (lockId != null && !lockId.isEmpty()) log(userInfo, LogData.L42_1, logger, lockId, StringUtils.join(pathList, ", "));
            return false;
        }
        return true;
    }


    private boolean checkPath(String path) {
        if (path == null || !FileWrapper.canReadFolder(path + "/") || !FileWrapper.canWriteFolder(path + "/"))
            return false;
        try {
            ResourceUtils.getSharedResource(path + "/");
        } catch (Exception e) {
			LOG.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public ImportCounter importRefBookNsi(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync) {
        ImportCounter importCounter = new ImportCounter();
        try {
            // ОКАТО
            importCounter.add(importRefBook(userInfo, logger, ConfigurationParam.OKATO_UPLOAD_DIRECTORY,
                    nsiOkatoMappingMap, OKATO_NAME, false, loadedFileNameList, lockId, isAsync));
            // Субъекты РФ
            importCounter.add(importRefBook(userInfo, logger, ConfigurationParam.REGION_UPLOAD_DIRECTORY,
                    nsiRegionMappingMap, REGION_NAME, false, loadedFileNameList, lockId, isAsync));
            // План счетов
            importCounter.add(importRefBook(userInfo, logger, ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY,
                    nsiAccountPlanMappingMap, ACCOUNT_PLAN_NAME, false, loadedFileNameList, lockId, isAsync));
        } catch (Exception e) {
            // Сюда должны попадать только при общих ошибках при импорте справочников, ошибки конкретного справочника перехватываются в сервисе
            logger.error(IMPORT_REF_BOOK_ERROR, NSI_NAME, e.getMessage());
            return importCounter;
        }
        log(userInfo, LogData.L24, logger, lockId, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    @Override
    public ImportCounter importRefBookDiasoft(TAUserInfo userInfo, Logger logger, String lock, boolean isAsync) {
        return importRefBookDiasoft(userInfo, null, logger, lock, isAsync);
    }

    @Override
    public ImportCounter importRefBookDiasoft(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync) {
        ImportCounter importCounter = new ImportCounter();
        try {
            importCounter = importRefBook(userInfo, logger, ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY,
                    diasoftMappingMap, DIASOFT_NAME, true, loadedFileNameList, lockId, isAsync);
        } catch (Exception e) {
            // Сюда должны попадать только при общих ошибках при импорте справочников, ошибки конкретного справочника перехватываются в сервисе
            logger.error(IMPORT_REF_BOOK_ERROR, DIASOFT_NAME, e.getMessage());
            return importCounter;
        }
        log(userInfo, LogData.L24, logger, lockId, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }


    public void getRefBookDiasoftFiles(List<TransportFileInfo> files, TAUserInfo userInfo, Logger logger, String lock) {
        try {
            getRefBookFiles(files, userInfo, logger, ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY,
                    diasoftMappingMap, null, lock);
        } catch (Exception e) {
        }
    }

    public void getRefBookAvgCostFiles(List<TransportFileInfo> files, TAUserInfo userInfo, Logger logger, String lock) {
        try {
            getRefBookFiles(files, userInfo, logger, ConfigurationParam.AVG_COST_UPLOAD_DIRECTORY,
                    avgCostMappingMap, null, lock);
        } catch (Exception e) {
        }
    }

    @Override
    public ImportCounter importRefBookAvgCost(TAUserInfo userInfo, Logger logger, String lock, boolean isAsync) {
        return importRefBookAvgCost(userInfo, null, logger, lock, isAsync);
    }

    @Override
    public ImportCounter importRefBookAvgCost(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, String lockId, boolean isAsync) {
        ImportCounter importCounter = new ImportCounter();
        try {
            importCounter = importRefBook(userInfo, logger, ConfigurationParam.AVG_COST_UPLOAD_DIRECTORY,
                    avgCostMappingMap, AVG_COST_NAME, true, loadedFileNameList, lockId, isAsync);
        } catch (Exception e) {
            // Сюда должны попадать только при общих ошибках при импорте справочников, ошибки конкретного справочника перехватываются в сервисе
            logger.error(IMPORT_REF_BOOK_ERROR, AVG_COST_NAME, e.getMessage());
            return importCounter;
        }
        log(userInfo, LogData.L24, logger, lockId, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    @Override
    public void saveRefBookRecords(long refBookId, Long uniqueRecordId, Long recordId, List<Map<String, RefBookValue>> saveRecords, Date validDateFrom,
                                   Date validDateTo, boolean isNewRecords, TAUserInfo userInfo, Logger logger) {
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("uniqueRecordId", uniqueRecordId);
        additionalParameters.put("recordCommonId", recordId);
        additionalParameters.put("saveRecords", saveRecords);
        additionalParameters.put("validDateFrom", validDateFrom);
        additionalParameters.put("validDateTo", validDateTo);
        additionalParameters.put("isNewRecords", isNewRecords);
        additionalParameters.put("scriptStatusHolder", new ScriptStatusHolder()); // Статус пока не обрабатывается
        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.SAVE, logger, additionalParameters);
    }

    @Override
    public void checkImportRefBookTransportData(TAUserInfo userInfo, Logger logger, String lockId, Date lockDate, boolean isAsync) {
        log(userInfo, LogData.L23, logger, lockId);
        if (checkPathArchiveError(userInfo, logger, lockId)){
            // Diasoft
            lockService.updateState(lockId, lockDate, "Импорт справочников \"Diasoft\"");
            importRefBookDiasoft(userInfo, logger, lockId, isAsync);
            // Средняя стоимость транспортных средств
            lockService.updateState(lockId, lockDate, "Импорт справочника \"Средняя стоимость транспортных средств\"");
            importRefBookAvgCost(userInfo, logger, lockId, isAsync);
        }
    }

    @Override
    public List<TransportFileInfo> getRefBookTransportDataFiles(TAUserInfo userInfo, Logger logger) {
        List<TransportFileInfo> files = new ArrayList<TransportFileInfo>();
        if (checkPathArchiveError(userInfo, logger, "")){
            // Diasoft
            getRefBookDiasoftFiles(files, userInfo, logger, "");
            // Средняя стоимость транспортных средств
            getRefBookAvgCostFiles(files, userInfo, logger, "");
        }
        return files;
    }

    @Override
    public void checkImportRefBooks(TAUserInfo userInfo, Logger logger, String uuid, boolean isAsync) {
        log(userInfo, LogData.L23, logger, uuid);
        if (checkPathArchiveError(userInfo, logger, uuid)){
            // Импорт справочников из ЦАС НСИ
            importRefBookNsi(userInfo, logger, uuid, isAsync);
            // Импорт справочников из Diasoft Custody
            importRefBookDiasoft(userInfo, logger, uuid, isAsync);
            // Импорт справочников в справочник "Средняя стоимость транспортных средств"
            importRefBookAvgCost(userInfo, logger, uuid, isAsync);
        }
    }

    /**
     * Проверка строки на соответствие регулярке из набора
     */
    private String mappingMatch(String name, Set<String> mappingSet) {
        name = name.toLowerCase();
        for (String key : mappingSet) {
            if (name.matches(key)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Путь к каталогу архива справочников
     */
    private String getRefBookArchivePath(TAUserInfo userInfo, Logger logger, String lockId) {
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> pathList = model.get(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY, 0);
        if (pathList == null || pathList.isEmpty()) {
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Путь к каталогу ошибок справочников
     */
    private String getRefBookErrorPath(TAUserInfo userInfo, Logger logger, String lockId) {
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> pathList = model.get(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY, 0);
        if (pathList == null || pathList.isEmpty()) {
            return null;
        }
        return pathList.get(0);
    }

    @Override
    public boolean isNSIFile(String name) {
        return name != null &&
                (mappingMatch(name, nsiOkatoMappingMap.keySet()) != null
                        || mappingMatch(name, nsiRegionMappingMap.keySet()) != null
                        || mappingMatch(name, nsiAccountPlanMappingMap.keySet()) != null);
    }

    @Override
    public boolean isDiasoftFile(String name) {
        return name != null && mappingMatch(name, diasoftMappingMap.keySet()) != null;
    }

    @Override
    public boolean isAvgCostFile(String name) {
        return name != null && mappingMatch(name, avgCostMappingMap.keySet()) != null;
    }

    /**
     * Получение спика ТФ НФ из каталога загрузки. Файлы, которые не соответствуют маппингу пропускаются.
     */
    private List<String> getWorkTransportFiles(TAUserInfo userInfo, String folderPath, Set<String> ignoreFileSet,
                                               Set<String> mappingSet, List<String> loadedFileNameList,
                                               List<String> errorFileList, Logger logger, ImportCounter wrongImportCounter, String lockId) {
        List<String> retVal = new LinkedList<String>();
        FileWrapper catalogFile = ResourceUtils.getSharedResource(folderPath + "/");
        for (String candidateStr : catalogFile.list()) {
            if (ignoreFileSet != null && ignoreFileSet.contains(candidateStr)) {
                continue;
            }

            if (loadedFileNameList != null && !loadedFileNameList.contains(candidateStr)) {
                // Если задан список определенных имен, а имя не из списка, то не загружаем такой файл
                continue;
            }

            // Это файл, а не директория и соответствует формату имени ТФ
            FileWrapper candidateFile = ResourceUtils.getSharedResource(folderPath + "/" + candidateStr);
            if (candidateFile.isFile()) {
                if (mappingMatch(candidateStr, mappingSet) != null) {
                    retVal.add(candidateStr);
                } else {
                    if (lockId != null && !lockId.isEmpty()) log(userInfo, LogData.L4, logger, lockId, candidateStr, folderPath);
                    errorFileList.add(candidateStr);
                    wrongImportCounter.add(new ImportCounter(0, 1));
                }
            }
        }
        // Система сортирует файлы по возрастанию по значению блоков VVV.RR в имени файла.
        Collections.sort(retVal);
        return retVal;
    }
}