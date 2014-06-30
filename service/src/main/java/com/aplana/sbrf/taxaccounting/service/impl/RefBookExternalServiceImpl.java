package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Arrays.asList;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class RefBookExternalServiceImpl implements RefBookExternalService {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private RefBookScriptingService refBookScriptingService;

    @Autowired
    private RefBookFactory refBookFactory;

    private static long REF_BOOK_OKATO = 3L; // Коды ОКАТО
    // private static long REF_BOOK_OUKS = 9L;  // Организации-участники контролируемых сделок
    private static long REF_BOOK_RF_SUBJ_CODE = 4L; // Коды субъектов Российской Федерации
    private static long REF_BOOK_EMITENT = 100L; // Эмитенты
    private static long REF_BOOK_BOND = 84L; // Ценные бумаги

    /**
     * Импорт справочников из папки
     * @param userInfo Пользователь
     * @param logger Логгер
     * @param refBookDirectoryParam Путь к директории
     * @param mappingMap Маппинг имен: Регулярка → Пара(Признак архива, Id справочника)
     */
    private void importRefBook(TAUserInfo userInfo, Logger logger, ConfigurationParam refBookDirectoryParam, Map<String, List<Pair<Boolean, Long>>> mappingMap) {
        // TODO добавить проверку ЭЦП (Marat Fayzullin 2013-10-19)
        ConfigurationParamModel model = configurationService.getAllConfig(userInfo);
        List<String> refBookDirectoryList = model.get(refBookDirectoryParam, DepartmentType.ROOT_BANK.getCode());

        BufferedReader reader = null;

        if (refBookDirectoryList == null || refBookDirectoryList.isEmpty()) {
            throw new ServiceException("Не указан путь к директории для импорта справочников.");
        }

        for (String refBookDirectory : refBookDirectoryList) {
            logger.info("Импорт данных справочников из директории «" + refBookDirectory + "».");

            refBookDirectory = refBookDirectory.trim();

            // Число успешно импортированных файлов
            int refBookImportCount = 0;

            // Признак наличия ошибок при импорте
            boolean withError = false;
            try {
                // SmbFile folder = new SmbFile(refBookDirectory);
                FileWrapper folder = ResourceUtils.getSharedResource(refBookDirectory);
                if (folder.list() != null) {
                    for (String fileName : folder.list()) {
                        FileWrapper file = ResourceUtils.getSharedResource(refBookDirectory + fileName);
                        // Из директории считываем только файлы
                        if (!file.isFile()) {
                            continue;
                        }
                        for (String key : mappingMap.keySet()) {
                            if (fileName.matches(key)) { // Нашли в мапе соответствие
                                List<Pair<Boolean, Long>> matchList = mappingMap.get(key);
                                for (Pair<Boolean, Long> refBookMapPair : matchList) {
                                    InputStream is = null;
                                    Long refBookId = refBookMapPair.getSecond();
                                    RefBook refBook = refBookFactory.get(refBookId);

                                    ScriptStatusHolder scriptStatusHolder = new ScriptStatusHolder();
                                    try {
                                        is = new BufferedInputStream(file.getInputStream());
                                        if (!refBookMapPair.getFirst()) {  // Если это не сам файл, а архив
                                            ZipInputStream zis = new ZipInputStream(is);
                                            ZipEntry zipFileName = zis.getNextEntry();
                                            if (zipFileName != null) { // в архиве есть файл
                                                // дальше работаем с первым файлом архива вместо самого архива
                                                is = zis;
                                            }
                                        }
                                        logger.info("Импорт данных справочника «" + refBook.getName() + "» из файла «" + fileName + "».");

                                        // Обращение к скрипту
                                        Map<String, Object> additionalParameters = new HashMap<String, Object>();
                                        additionalParameters.put("inputStream", is);
                                        additionalParameters.put("fileName", fileName);
                                        additionalParameters.put("scriptStatusHolder", scriptStatusHolder);
                                        refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT, logger, additionalParameters);
                                        // Обработка результата выполнения скрипта
                                        switch (scriptStatusHolder.getScriptStatus()) {
                                            case SUCCESS:
                                                logger.info("Импорт успешно выполнен.");
                                                refBookImportCount++;
                                                break;
                                            case SKIP:
                                                logger.info("Файл пропущен. " + scriptStatusHolder.getStatusMessage());
                                                break;
                                        }
                                    } catch (Exception e) {
                                        //// Ошибка импорта отдельного справочника — откатываются изменения только по нему, импорт продолжается
                                        withError = true;
                                        String errorMsg;
                                        if (e != null && e.getLocalizedMessage() != null) {
                                            errorMsg = e.getLocalizedMessage() + ".";
                                        } else {
                                            errorMsg = "";
                                        }

                                        errorMsg = "Не удалось выполнить импорт данных справочника «" + refBook.getName()
                                                + "» из файла «" + fileName + "». " + errorMsg;

                                        // Журнал аудита
                                        auditService.add(FormDataEvent.IMPORT, userInfo, userInfo.getUser().getDepartmentId(),
                                                null, null, null, null, errorMsg);

                                        logger.error(errorMsg);
                                    } finally {
                                        IOUtils.closeQuietly(is);
                                    }
                                }
                            }
                        }
                    }
                }
                String msg = "Произведен импорт данных справочников из «" + refBookDirectory + "»" +
                        (withError ? " с ошибками." : " без ошибок.") + " Импортировано файлов: " + refBookImportCount + ".";

                if (refBookImportCount == 0) {
                    msg = "Импорт не выполнен, корректных файлов с данными справочников в папке «" + refBookDirectory + "» не найдено.";
                }

                // Журнал аудита
                auditService.add(FormDataEvent.IMPORT, userInfo, userInfo.getUser().getDepartmentId(), null, null, null,
                        null, msg);
                logger.info(msg);
            } catch (Exception e) {
                //// Глобальная ошибка импорта — все изменения откатываются
                // Журнал аудита
                String errorMsg;
                if (e != null && e.getLocalizedMessage() != null) {
                    errorMsg = e.getLocalizedMessage() + ".";
                } else {
                    errorMsg = "";
                }
                errorMsg = "Импорт не выполнен, ошибка доступа к папке «" + refBookDirectory + "». " + errorMsg;

                auditService.add(FormDataEvent.IMPORT, userInfo, userInfo.getUser().getDepartmentId(), null, null, null,
                        null, errorMsg);
                throw new ServiceException(errorMsg, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    @Override
    public void importRefBook(TAUserInfo userInfo, Logger logger) {
        // регулярка файла/архива - true/false - id
        Map<String, List<Pair<Boolean, Long>>> mappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();
        // архив для загр. спр. "Коды ОКАТО"
        mappingMap.put("OKA.*", asList(new Pair<Boolean, Long>(false, REF_BOOK_OKATO)));
        // архив для загр. спр. "Коды ОКАТО"
        mappingMap.put("payments.*", asList(new Pair<Boolean, Long>(false, REF_BOOK_OKATO)));
        // ОКАТО
        importRefBook(userInfo, logger, ConfigurationParam.OKATO_TRANSPORT_DIRECTORY, mappingMap);

        mappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();
        // архив для загр. спр. "Коды субъектов Российской Федерации" (Регионы)
        mappingMap.put("RNU.*", asList(new Pair<Boolean, Long>(false, REF_BOOK_RF_SUBJ_CODE)));
        // архив для загр. спр. "Коды субъектов Российской Федерации" (Регионы)
        mappingMap.put("generaluse.AS_RNU.*.*", asList(new Pair<Boolean, Long>(false, REF_BOOK_RF_SUBJ_CODE)));
        // Субъекты РФ
        importRefBook(userInfo, logger, ConfigurationParam.OKATO_TRANSPORT_DIRECTORY, mappingMap);
    }

    @Override
    public void importRefBookDiasoft(TAUserInfo userInfo, Logger logger) {
        // Регулярка → Пара(Признак архива, Id справочника)
        Map<String, List<Pair<Boolean, Long>>> mappingMap = new HashMap<String, List<Pair<Boolean, Long>>>();
        // Ценные бумаги + Эмитенты
        mappingMap.put("DS[0-9]{6}\\.nsi", asList(new Pair<Boolean, Long>(true, REF_BOOK_EMITENT), new Pair<Boolean, Long>(true, REF_BOOK_BOND)));

        // TODO Вопросы по аналитике, пока нет ясности как хранить параметр http://conf.aplana.com/pages/viewpage.action?pageId=13112570
        //importRefBook(userInfo, logger, ConfigurationParam.REF_BOOK_DIASOFT_DIRECTORY, mappingMap);
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
}