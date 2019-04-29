package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
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
    private LockDataService lockService;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private AsyncTaskDao asyncTaskDao;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private ValidateXMLService validateXMLService;

    /**
     * Максимальное количество попыток создания временной директории
     */
    private static final int TEMP_DIR_ATTEMPT_MAX_COUNT = 9;
    // ФИАС
    private static final long REF_BOOK_FIAS = RefBook.Id.FIAS_ADDR_OBJECT.getId(); // ФИАС
    private static final String FIAS_NAME = "справочника ФИАС";
    private static final String LOCK_MESSAGE = "Справочник «%s» заблокирован, попробуйте выполнить операцию позже!";

    //// Справочник ФИАС
    private static final Map<String, List<Pair<Boolean, Long>>> fiasMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();

    // Сообщения, которые не учтены в постановка
    private static final String IMPORT_REF_BOOK_ERROR = "Ошибка при загрузке транспортных файлов %s. %s";

    static {
        // Файл «ФИАС»
        fiasMappingMap.put("fias_xml\\.rar", asList(new Pair<>(true, REF_BOOK_FIAS)));

        // Инициализации библиотеки
        try {
            final File tmpDirectory = createTempDir("SevenZipLib");
            try {
                SevenZip.initSevenZipFromPlatformJAR(tmpDirectory);
            } catch (SevenZipNativeInitializationException e) {
                LOG.error("", e);
            }
            Runtime.getRuntime().addShutdownHook(new Thread("Fias cleanup") {
                @Override
                public void run() {
                    try {
                        FileUtils.deleteDirectory(tmpDirectory);
                    } catch (IOException e) {
                        LOG.error("", e);
                    }
                }
            });
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    @Override
    public ImportCounter importRefBookFias(TAUserInfo userInfo, List<String> loadedFileNameList, Logger logger, long taskId) {
        ImportCounter importCounter = new ImportCounter();
        log(userInfo, LogData.L23, logger, taskId);
        if (checkPathArchiveError(userInfo, logger, taskId)) {
            asyncManager.updateState(taskId, AsyncTaskState.FIAS_IMPORT);
            try {
                long maxFileSize = asyncTaskTypeDao.findById(AsyncTaskType.LOAD_ALL_TRANSPORT_DATA.getId()).getTaskLimit();
                importCounter = importRefBook(userInfo, logger, ConfigurationParam.FIAS_UPLOAD_DIRECTORY,
                        fiasMappingMap, FIAS_NAME, true, loadedFileNameList, taskId, maxFileSize);
            } catch (Exception e) {
                // Сюда должны попадать только при общих ошибках при импорте справочников, ошибки конкретного справочника перехватываются в сервисе
                LOG.error(e.getLocalizedMessage(), e);
                logger.error(IMPORT_REF_BOOK_ERROR, FIAS_NAME, e.getMessage());
                return importCounter;
            }
            log(userInfo, LogData.L24, logger, taskId, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        }
        return importCounter;
    }

    @Override
    public void saveRefBookRecords(long refBookId, Long uniqueRecordId, Long recordId, Long sourceUniqueRecordId, List<Map<String, RefBookValue>> saveRecords, Date validDateFrom,
                                   Date validDateTo, boolean isNewRecords, TAUserInfo userInfo, Logger logger) {
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("uniqueRecordId", uniqueRecordId);
        additionalParameters.put("recordCommonId", recordId);
        additionalParameters.put("sourceUniqueRecordId", sourceUniqueRecordId);
        additionalParameters.put("saveRecords", saveRecords);
        additionalParameters.put("validDateFrom", validDateFrom);
        additionalParameters.put("validDateTo", validDateTo);
        additionalParameters.put("isNewRecords", isNewRecords);
        additionalParameters.put("scriptStatusHolder", new ScriptStatusHolder()); // Статус пока не обрабатывается
        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.SAVE, logger, additionalParameters);
    }

    private static File createTempDir(String prefix) throws IOException {
        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        File newTempDir;
        int attemptCount = 0;
        do {
            attemptCount++;
            if (attemptCount > TEMP_DIR_ATTEMPT_MAX_COUNT) {
                throw new IOException("Не удалось создать уникальный временный каталог после " + TEMP_DIR_ATTEMPT_MAX_COUNT + " попыток.");
            }
            String dirName = prefix + System.nanoTime();
            newTempDir = new File(sysTempDir, dirName);
        } while (newTempDir.exists());
        if (newTempDir.mkdirs()) {
            return newTempDir;
        }
        throw new IOException("Не удалось создать временный каталог с именем " + newTempDir.getAbsolutePath());
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
    ImportCounter importRefBook(TAUserInfo userInfo, Logger logger, ConfigurationParam refBookDirectoryParam,
                                Map<String, List<Pair<Boolean, Long>>> mappingMap, String refBookName, boolean move,
                                List<String> loadedFileNameList, long taskId, long maxFileSize) {
        // Получение пути к каталогу загрузки ТФ
        ConfigurationParamModel model = configurationDao.fetchAllByDepartment(0);
        List<String> refBookDirectoryList = model.get(refBookDirectoryParam, 0);
        if (refBookDirectoryList == null || refBookDirectoryList.isEmpty()) {
            log(userInfo, LogData.L37, logger, taskId, refBookName);
            return new ImportCounter();
        }

        // Счетчики
        int success = 0;
        int fail = 0;

        ImportCounter wrongImportCounter = new ImportCounter();
        // Каталогов может быть несколько, хоть сейчас в ConfigurationParam и ограничено одним значением для всех справочников
        for (String path : refBookDirectoryList) {
            if (!checkPath(path)) {
                log(userInfo, LogData.L42_2, logger, taskId, path, refBookName);
                continue;
            }
            // Набор файлов, которые уже обработали
            Set<String> ignoreFileSet = new HashSet<String>();
            // Файлы которые надо перенести в каталог ошибок
            List<String> errorFileList = new ArrayList<String>();
            // Если изначально нет подходящих файлов то выдаем отдельную ошибку
            List<String> workFilesList = getWorkTransportFiles(userInfo, path, ignoreFileSet, mappingMap.keySet(),
                    loadedFileNameList, errorFileList, logger, wrongImportCounter, taskId);

            if (workFilesList.isEmpty()) {
                log(userInfo, LogData.L31, logger, taskId, refBookName);
            }

            if (move) {
                for (String fileName : errorFileList) {
                    FileWrapper currentFile = ResourceUtils.getSharedResource(path + "/" + fileName);
                    if (currentFile.isFile()) {
                        moveToErrorDirectory(userInfo, getRefBookErrorPath(), currentFile, null, logger, taskId);
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
                    log(userInfo, LogData.L47, logger, taskId, fileName, currentFile.length() / 1024, path, maxFileSize);
                    if (move) {
                        moveToErrorDirectory(userInfo, getRefBookErrorPath(), currentFile, null, logger, taskId);
                    }
                    fail++;
                    continue;
                }
                // Блокировка файла
                LockData fileLock = lockService.lock(LockData.LockObjects.FILE.name() + "_" + fileName,
                        userInfo.getUser().getId(),
                        String.format(DescriptionTemplate.FILE.getText(), fileName));
                if (fileLock != null) {
                    log(userInfo, LogData.L41, logger, taskId, fileName);
                    fail++;
                    continue;
                }

                try {
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
                        IInArchive archive = null;
                        RandomAccessFile randomAccessFile = null;
                        try {
                            // Обращение к скрипту
                            Map<String, Object> additionalParameters = new HashMap<String, Object>();
                            additionalParameters.put("fileName", fileName);
                            additionalParameters.put("scriptStatusHolder", scriptStatusHolder);

                            //Устанавливаем блокировку на справочник
                            List<String> lockedObjects = new ArrayList<String>();
                            String lockKey = commonRefBookService.generateTaskKey(refBookId);
                            int userId = userInfo.getUser().getId();

                            RefBook refBook = refBookDao.get(refBookId);
                            LockData lockData = lockService.lock(lockKey, userId,
                                    String.format(DescriptionTemplate.REF_BOOK_EDIT.getText(), refBook.getName()));
                            if (lockData == null) {
                                try {
                                    //Блокировка установлена
                                    lockedObjects.add(lockKey);
                                    //Блокируем связанные справочники
                                    List<RefBookAttribute> attributes = refBook.getAttributes();
                                    for (RefBookAttribute attribute : attributes) {
                                        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                                            RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                                            String referenceLockKey = commonRefBookService.generateTaskKey(attribute.getRefBookId());
                                            if (!lockedObjects.contains(referenceLockKey)) {
                                                if (lockService.lock(referenceLockKey, userId, String.format(DescriptionTemplate.REF_BOOK_EDIT.getText(), attributeRefBook.getName())) == null) {
                                                    //Блокировка установлена
                                                    lockedObjects.add(referenceLockKey);
                                                } else {
                                                    throw new ServiceException(String.format(LOCK_MESSAGE, attributeRefBook.getName()));
                                                }
                                            }
                                        }
                                    }

                                    if (refBookMapPair.getSecond().equals(REF_BOOK_FIAS)) {
                                        randomAccessFile = new RandomAccessFile(currentFile.getFile(), "r");
                                        archive = SevenZip.openInArchive(ArchiveFormat.RAR, // null - autodetect
                                                new RandomAccessFileInStream(
                                                        randomAccessFile));

                                        additionalParameters.put("archive", archive); // обьект для работы с RAR-архивом
                                    } else {
                                        is = currentFile.getInputStream();
                                        if (!refBookMapPair.getFirst()) {  // Если это не сам файл, а архив
                                            ZipInputStream zis = new ZipInputStream(is);
                                            ZipEntry zipFileName = zis.getNextEntry();
                                            if (zipFileName != null) { // в архиве есть файл
                                                // дальше работаем с первым файлом архива вместо самого архива
                                                is = zis;
                                            }
                                        }
                                        additionalParameters.put("inputStream", is);
                                    }

                                    //Выполняем логику скрипта
                                    try {
                                        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT_TRANSPORT_FILE,
                                                localLoggerList.get(i), additionalParameters);
                                    } finally {
                                        IOUtils.closeQuietly(is);
                                        try {
                                            if (archive != null) {
                                                archive.close();
                                            }
                                        } catch (IOException ioe) {
                                            // ignore
                                        }
                                        if (randomAccessFile != null) {
                                            try {
                                                randomAccessFile.close();
                                            } catch (IOException e) {
                                                // ignore
                                            }
                                        }
                                    }
                                    // Обработка результата выполнения скрипта
                                    switch (scriptStatusHolder.getScriptStatus()) {
                                        case SUCCESS:
                                            // Уже загрузили, больше не пытаемся
                                            load = true;
                                            if (move) {
                                                // Перемещение в каталог архива
                                                int positionLogger = logger.getEntries().size();
                                                boolean result = moveToArchiveDirectory(userInfo, getRefBookArchivePath(), currentFile, logger, taskId);
                                                if (result) {
                                                    success++;
                                                    logger.getEntries().addAll(positionLogger, localLoggerList.get(i).getEntries());
                                                    log(userInfo, LogData.L20, logger, taskId, currentFile.getName());
                                                } else {
                                                    fail++;
                                                    // Если в архив не удалось перенести, то пытаемся перенести в каталог ошибок
                                                    moveToErrorDirectory(userInfo, getRefBookErrorPath(), currentFile,
                                                            Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L12.getText(), taskId, ""))), logger, taskId);
                                                }
                                            } else {
                                                success++;
                                                logger.getEntries().addAll(localLoggerList.get(i).getEntries());
                                                log(userInfo, LogData.L20, logger, taskId, currentFile.getName());
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
                            log(userInfo, LogData.L21, logger, taskId, e.getMessage());
                            // Перемещение в каталог ошибок
                            logger.getEntries().addAll(localLoggerList.get(i).getEntries());
                            if (move) {
                                moveToErrorDirectory(userInfo, getRefBookErrorPath(), currentFile,
                                        getEntries(localLoggerList), logger, taskId);
                            }
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                    }
                    if (skip == matchList.size()) {
                        // В случае неуспешного импорта в общий лог попадает вывод всех скриптов
                        logger.getEntries().addAll(getEntries(localLoggerList));
                        // Файл пропущен всеми справочниками — неправильный формат
                        log(userInfo, LogData.L38, logger, taskId, refBookName);
                        if (move) {
                            moveToErrorDirectory(userInfo, getRefBookErrorPath(), currentFile,
                                    getEntries(localLoggerList), logger, taskId);
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
     * Список сущностей из объединенных логов
     */
    private List<LogEntry> getEntries(List<Logger> loggerList) {
        List<LogEntry> retVal = new LinkedList<LogEntry>();
        for (Logger logger : loggerList) {
            retVal.addAll(logger.getEntries());
        }
        return retVal;
    }

    private boolean checkPathArchiveError(TAUserInfo userInfo, Logger logger, long taskId) {
        String archivePath = getRefBookArchivePath();
        String errorPath = getRefBookErrorPath();
        List<String> pathList = new ArrayList<String>();
        if (archivePath == null) {
            pathList.add("к каталогу архива");
        }
        if (errorPath == null) {
            pathList.add("к каталогу ошибок");
        }
        if (!pathList.isEmpty()) {
            log(userInfo, LogData.L43, logger, taskId, StringUtils.join(pathList, ", "));
            return false;
        }
        if (!checkPath(archivePath)) {
            pathList.add("к каталогу архива «" + archivePath + "»");
        }
        if (!checkPath(errorPath)) {
            pathList.add("к каталогу ошибок «" + errorPath + "»");
        }
        if (!pathList.isEmpty()) {
            log(userInfo, LogData.L42_1, logger, taskId, StringUtils.join(pathList, ", "));
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
    private String getRefBookArchivePath() {
        ConfigurationParamModel model = configurationDao.fetchAllByDepartment(0);
        List<String> pathList = model.get(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY, 0);
        if (pathList == null || pathList.isEmpty()) {
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Путь к каталогу ошибок справочников
     */
    private String getRefBookErrorPath() {
        ConfigurationParamModel model = configurationDao.fetchAllByDepartment(0);
        List<String> pathList = model.get(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY, 0);
        if (pathList == null || pathList.isEmpty()) {
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Получение спика ТФ НФ из каталога загрузки. Файлы, которые не соответствуют маппингу пропускаются.
     */
    private List<String> getWorkTransportFiles(TAUserInfo userInfo, String folderPath, Set<String> ignoreFileSet,
                                               Set<String> mappingSet, List<String> loadedFileNameList,
                                               List<String> errorFileList, Logger logger, ImportCounter wrongImportCounter, long taskId) {
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
                    log(userInfo, LogData.L4, logger, taskId, candidateStr, folderPath);
                    errorFileList.add(candidateStr);
                    wrongImportCounter.add(new ImportCounter(0, 1));
                }
            }
        }
        // Система сортирует файлы по возрастанию по значению блоков VVV.RR в имени файла.
        Collections.sort(retVal);
        return retVal;
    }

    @Override
    public void importXml(long refBookId, BlobData blobData, TAUserInfo userInfo, Logger logger) {
        File xmlFile = createTempFile(blobData.getInputStream());
        String fileName = blobData.getName();
        try {
            RefBook refBook = commonRefBookService.get(refBookId);
            validate(xmlFile, refBook, logger);
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException(
                        "Загрузка файла \"%s\" не может быть выполнена. Файл не соответствует xsd-схеме.",
                        logEntryService.save(logger.getEntries()),
                        fileName);
            }
            lockAndImportXml(refBookId, xmlFile, fileName, userInfo, logger);
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException(
                        "Загрузка файла \"%s\" не может быть выполнена.",
                        logEntryService.save(logger.getEntries()),
                        fileName);
            }
        } finally {
            if (xmlFile != null && !xmlFile.delete()) {
                LOG.warn("Не удален временный файл: " + xmlFile.getAbsoluteFile());
            }
        }
    }

    /**
     * Валидирует указанный xml-файл по xsd схеме справочника с использованием схематрона
     */
    private boolean validate(File xmlFile, RefBook refBook, Logger logger) {
        if (refBook.getXsdId() != null) {
            BlobData blobData = blobDataService.get(refBook.getXsdId());
            return validateXMLService.validate(
                    logger,
                    xmlFile.getName(),
                    xmlFile,
                    "personData.xsd",
                    blobData.getInputStream()
            );
        }
        return true;
    }

    private void lockAndImportXml(long refBookId, File xmlFile, String fileName, TAUserInfo userInfo, Logger logger) {
        TAUser user = userInfo.getUser();
        String refBookLockKey = commonRefBookService.generateTaskKey(refBookId);
        LockData refBookLockData = lockService.lock(refBookLockKey, user.getId(),
                commonRefBookService.getRefBookActionDescription(DescriptionTemplate.REF_BOOK_EDIT, refBookId));
        if (refBookLockData == null || refBookLockData.getUserId() == user.getId()) {
            try (InputStream xml = new BufferedInputStream(new FileInputStream(xmlFile))) {
                doImportXml(refBookId, xml, fileName, userInfo, logger);
            } catch (IOException e) {
                throw new ServiceException(e.getMessage(), e);
            } finally {
                lockService.unlock(refBookLockKey, user.getId());
            }
        } else {
            logger.error(commonRefBookService.getRefBookLockDescription(refBookLockData, refBookId));
        }
    }

    private void doImportXml(long refBookId, InputStream xml, String fileName, TAUserInfo userInfo, Logger logger) {
        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("inputStream", xml);
        additionalParameters.put("fileName", fileName);

        if (!refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT, logger, additionalParameters)) {
            throw new ServiceException();
        }
    }

    private File createTempFile(InputStream inputStream) {
        OutputStream out = null;
        File file;
        try {
            file = File.createTempFile("ref_book_import_", ".tmp");
            out = new BufferedOutputStream(new FileOutputStream(file));
            IOUtils.copy(inputStream, out);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(inputStream);
        }
        return file;
    }
}