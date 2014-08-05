package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.SignService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
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

    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private SignService signService;

    // ЦАС НСИ
    private static long REF_BOOK_OKATO = 3L; // Коды ОКАТО
    private static long REF_BOOK_RF_SUBJ_CODE = 4L; // Коды субъектов Российской Федерации
    private static long REF_BOOK_ACCOUNT_PLAN = 101L; // План счетов
    // Diasoft Custody
    private static long REF_BOOK_EMITENT = 100L; // Эмитенты
    private static long REF_BOOK_BOND = 84L; // Ценные бумаги

    private final String OKATO_NAME = "справочника ОКАТО";
    private final String REGION_NAME = "справочника «Субъекты РФ»";
    private final String ACCOUNT_PLAN_NAME = "справочника «План счетов»";
    private final String DIASOFT_NAME = "справочников Diasoft";

    //// Справочники ЦАС НСИ
    // ОКАТО
    private final static Map<String, List<Pair<Boolean, Long>>> nsiOkatoMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();
    // Субъекты РФ
    private final static Map<String, List<Pair<Boolean, Long>>> nsiRegionMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();
    // План счетов
    private final static Map<String, List<Pair<Boolean, Long>>> nsiAccountPlanMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();

    //// Справочники АС Diasoft Custody
    // Ценные бумаги и Эмитенты
    private final static Map<String, List<Pair<Boolean, Long>>> diasoftMappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();

    // Сообщения, которые не учтены в постановка
    final static String IMPORT_REF_BOOK_ERROR = "Ошибка при загрузке транспортных файлов справочников %s.";

    static {
        // TODO Левыкин: Каждый конкретный справочник будет загружаться только архивом или только простым файлом, не обоими способами

        // Ценные бумаги + Эмитенты
        diasoftMappingMap.put("ds\\d{6}\\.nsi", asList(new Pair<Boolean, Long>(true, REF_BOOK_EMITENT),
                new Pair<Boolean, Long>(true, REF_BOOK_BOND)));

        // Архив "Коды ОКАТО"
        nsiOkatoMappingMap.put("oka.{5}\\..{2}", asList(new Pair<Boolean, Long>(false, REF_BOOK_OKATO)));
        // "Коды ОКАТО"
        nsiOkatoMappingMap.put("payments\\.okato\\..{4}\\..{3}\\..{2}", asList(new Pair<Boolean, Long>(true, REF_BOOK_OKATO)));

        // Архив "Коды субъектов Российской Федерации" (Регионы)
        nsiRegionMappingMap.put("rnu.{5}\\..{2}", asList(new Pair<Boolean, Long>(false, REF_BOOK_RF_SUBJ_CODE)));
        // "Коды субъектов Российской Федерации" (Регионы)
        nsiRegionMappingMap.put("generaluse\\.as_rnu\\..{3}\\..{2}", asList(new Pair<Boolean, Long>(true, REF_BOOK_RF_SUBJ_CODE)));

        // Архив «План счетов»
        nsiAccountPlanMappingMap.put("bookkeeping\\.bookkeeping\\..{3}\\..{2}", asList(new Pair<Boolean, Long>(false, REF_BOOK_ACCOUNT_PLAN)));
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
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    private ImportCounter importRefBook(TAUserInfo userInfo, Logger logger, ConfigurationParam refBookDirectoryParam,
                                        Map<String, List<Pair<Boolean, Long>>> mappingMap, String refBookName, boolean move) {
        // Получение пути к каталогу загрузки ТФ
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> refBookDirectoryList = model.get(refBookDirectoryParam, 0);
        if (refBookDirectoryList == null || refBookDirectoryList.isEmpty()) {
            log(userInfo, LogData.L31, logger, refBookName);
            return new ImportCounter();
        }

        // Счетчики
        int success = 0;
        int fail = 0;

        // Каталогов может быть несколько, хоть сейчас в ConfigurationParam и ограничено одним значением для всех справочников
        for (String path : refBookDirectoryList) {
            // Набор файлов, которые уже обработали
            Set<String> ignoreFileSet = new HashSet<String>();
            // Если изначально нет подходящих файлов то выдаем отдельную ошибку
            List<String> workFilesList = getWorkTransportFiles(path, ignoreFileSet, mappingMap.keySet());

            if (workFilesList.isEmpty()) {
                log(userInfo, LogData.L31, logger, refBookName);
                return new ImportCounter();
            }

            // Обработка всех подходящих файлов, с получением списка на каждой итерации
            for (String fileName : workFilesList) {
                ignoreFileSet.add(fileName);
                FileWrapper currentFile = ResourceUtils.getSharedResource(path + fileName);

                // ЭЦП
                List<String> signList = configurationDao.getByDepartment(0).get(ConfigurationParam.SIGN_CHECK, 0);
                if (signList != null && !signList.isEmpty() && signList.get(0).equals("1")) {
                    boolean check = false;
                    try {
                        check = signService.checkSign(currentFile.getPath(), 0);
                    } catch (Exception e) {
                        logger.error("Ошибка при проверке ЭЦП: " + e.getMessage());
                    }
                    if (!check) {
                        log(userInfo, LogData.L16, logger, fileName);
                        return new ImportCounter();
                    }
                    log(userInfo, LogData.L15, logger, fileName);
                } else {
                    log(userInfo, LogData.L15_1, logger, fileName);
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
                                    boolean result = moveToArchiveDirectory(userInfo, getRefBookArchivePath(userInfo,
                                            logger), currentFile, logger);
                                    if (result) {
                                        success++;
                                        logger.getEntries().addAll(localLoggerList.get(i).getEntries());
                                        log(userInfo, LogData.L20, logger, currentFile.getName());
                                    } else {
                                        fail++;
                                        // Если в архив не удалось перенести, то пытаемся перенести в каталог ошибок
                                        moveToErrorDirectory(userInfo, getRefBookErrorPath(userInfo, logger), currentFile,
                                                Arrays.asList(new LogEntry(LogLevel.ERROR, String.format(LogData.L12.getText(), ""))), logger);
                                    }
                                } else {
                                    success++;
                                    logger.getEntries().addAll(localLoggerList.get(i).getEntries());
                                    log(userInfo, LogData.L20, logger, currentFile.getName());
                                }
                                break;
                            case SKIP:
                                skip++;
                                if (skip == matchList.size()) {
                                    // В случае неуспешного импорта в общий лог попадает вывод всех скриптов
                                    logger.getEntries().addAll(getEntries(localLoggerList));
                                    // Файл пропущен всеми справочниками — неправильный формат
                                    log(userInfo, LogData.L4, logger);
                                }
                                break;
                        }
                    } catch (Exception e) {
                        // При ошибке второй раз не пытаемся загрузить
                        load = true;
                        IOUtils.closeQuietly(is);
                        fail++;
                        // Ошибка импорта отдельного справочника — откатываются изменения только по нему, импорт продолжается
                        log(userInfo, LogData.L21, logger, e.getMessage());
                        // Перемещение в каталог ошибок
                        logger.getEntries().addAll(getEntries(localLoggerList));
                        if (move) {
                            moveToErrorDirectory(userInfo, getRefBookErrorPath(userInfo, logger), currentFile,
                                    getEntries(localLoggerList), logger);
                        }
                    }
                }
            }
        }
        return new ImportCounter(success, fail);
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
    public ImportCounter importRefBookNsi(TAUserInfo userInfo, Logger logger) {
        log(userInfo, LogData.L23, logger);
        ImportCounter importCounter = new ImportCounter();
        try {
            // ОКАТО
            importCounter.add(importRefBook(userInfo, logger, ConfigurationParam.OKATO_UPLOAD_DIRECTORY,
                    nsiOkatoMappingMap, OKATO_NAME, false));
            // Субъекты РФ
            importCounter.add(importRefBook(userInfo, logger, ConfigurationParam.REGION_UPLOAD_DIRECTORY,
                    nsiRegionMappingMap, REGION_NAME, false));
            // План счетов
            importCounter.add(importRefBook(userInfo, logger, ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY,
                    nsiAccountPlanMappingMap, ACCOUNT_PLAN_NAME, false));
        } catch (Exception e) {
            // Сюда должны попадать только при общих ошибках при импорте справочников, ошибки конкретного справочника перехватываются в сервисе
            logger.error(IMPORT_REF_BOOK_ERROR, e.getMessage());
            return importCounter;
        }
        log(userInfo, LogData.L24, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    @Override
    public ImportCounter importRefBookDiasoft(TAUserInfo userInfo, Logger logger) {
        log(userInfo, LogData.L23, logger);
        ImportCounter importCounter = new ImportCounter();
        try {
            importCounter = importRefBook(userInfo, logger, ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY,
                    diasoftMappingMap, DIASOFT_NAME, true);
        } catch (Exception e) {
            // Сюда должны попадать только при общих ошибках при импорте справочников, ошибки конкретного справочника перехватываются в сервисе
            logger.error(IMPORT_REF_BOOK_ERROR, e.getMessage());
            return importCounter;
        }
        log(userInfo, LogData.L24, logger, importCounter.getSuccessCounter(), importCounter.getFailCounter());
        return importCounter;
    }

    @Override
    public void saveRefBookRecords(long refBookId, List<Map<String, RefBookValue>> saveRecords, Date validDateFrom,
                                   Date validDateTo, boolean isNewRecords, TAUserInfo userInfo, Logger logger) {
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("saveRecords", saveRecords);
        additionalParameters.put("validDateFrom", validDateFrom);
        additionalParameters.put("validDateTo", validDateTo);
        additionalParameters.put("isNewRecords", isNewRecords);
        additionalParameters.put("scriptStatusHolder", new ScriptStatusHolder()); // Статус пока не обрабатывается
        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.SAVE, logger, additionalParameters);
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
    private String getRefBookArchivePath(TAUserInfo userInfo, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> pathList = model.get(ConfigurationParam.REF_BOOK_ARCHIVE_DIRECTORY, 0);
        if (pathList == null || pathList.isEmpty()) {
            log(userInfo, LogData.L_2, logger);
            return null;
        }
        return pathList.get(0);
    }

    /**
     * Путь к каталогу ошибок справочников
     */
    private String getRefBookErrorPath(TAUserInfo userInfo, Logger logger) {
        ConfigurationParamModel model = configurationDao.getByDepartment(0);
        List<String> pathList = model.get(ConfigurationParam.REF_BOOK_ERROR_DIRECTORY, 0);
        if (pathList == null || pathList.isEmpty()) {
            log(userInfo, LogData.L_1, logger);
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

    /**
     * Получение спика ТФ НФ из каталога загрузки. Файлы, которые не соответствуют маппингу пропускаются.
     */
    private List<String> getWorkTransportFiles(String folderPath, Set<String> ignoreFileSet, Set<String> mappingSet) {
        List<String> retVal = new LinkedList<String>();
        FileWrapper catalogFile = ResourceUtils.getSharedResource(folderPath);
        for (String candidateStr : catalogFile.list()) {
            if (ignoreFileSet != null && ignoreFileSet.contains(candidateStr)) {
                continue;
            }
            // Это файл, а не директория и соответствует формату имени ТФ
            if (mappingMatch(candidateStr, mappingSet) != null) {
                FileWrapper candidateFile = ResourceUtils.getSharedResource(folderPath + candidateStr);
                if (candidateFile.isFile()) {
                    retVal.add(candidateStr);
                }
            }
        }
        // Система сортирует файлы по возрастанию по значению блоков VVV.RR в имени файла.
        Collections.sort(retVal);
        return retVal;
    }
}