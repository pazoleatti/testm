package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class RefBookExternalServiceImpl implements RefBookExternalService {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private RefBookScriptingService refBookScriptingService;

    @Override
    public void importRefBook(TAUserInfo userInfo, Logger logger) {
        // регулярка файла/архива - true/false - id
        Map<String, Pair<Boolean, Long>> map = new HashMap<String, Pair<Boolean, Long>>();
        // архив для загр. спр. "Коды ОКАТО"
        map.put("OKA.*", new Pair<Boolean, Long>(false, 3L));
        // архив для загр. спр. "Коды ОКАТО"
        map.put("payments.*", new Pair<Boolean, Long>(false, 3L));
        // файл для загр. спр. "Организации-участники контролируемых сделок"
        map.put("organization.xls", new Pair<Boolean, Long>(true, 9L));
        // архив для загр. спр. "Коды субъектов Российской Федерации" (Регионы)
        map.put("RNU.*", new Pair<Boolean, Long>(false, 4L));
        // архив для загр. спр. "Коды субъектов Российской Федерации" (Регионы)
        map.put("generaluse.AS_RNU.*.*", new Pair<Boolean, Long>(false, 4L));

        //TODO добавить проверку ЭЦП (Marat Fayzullin 2013-10-19)
        Map<ConfigurationParam, String> params = configurationService.getAllConfig(userInfo);
        String refBookDirectory = params.get(ConfigurationParam.REF_BOOK_DIRECTORY);
        // String refBookDirectory = "file://c:/okato/";
        // String refBookDirectory = "file://c:/region/";

        BufferedReader reader = null;
        logger.info("Импорт данных справочников из директории \"" + refBookDirectory + "\".");

        if (refBookDirectory == null || refBookDirectory.trim().isEmpty()) {
            throw new ServiceException("Не указан путь к директории для импорта справочников.");
        }

        refBookDirectory = refBookDirectory.trim();

        // Число успешно импортированных файлов
        int refBookImportCount = 0;

        // Признак наличия ошибок при импорте
        boolean withError = false;
        try {
            // SmbFile folder = new SmbFile(refBookDirectory);
            FileWrapper folder = ResourceUtils.getSharedResource(refBookDirectory);
            for (String fileName : folder.list()) {
                FileWrapper file = ResourceUtils.getSharedResource(refBookDirectory + fileName);
                // Из директории считываем только файлы
                if (!file.isFile()) {
                    continue;
                }
                for (String key : map.keySet()) {
                    if (fileName.matches(key)) { // Нашли в мапе соответствие
                        InputStream is = null;
                        Long refBookId = map.get(key).getSecond();
                        try {
                            is = new BufferedInputStream(file.getStream());
                            if (!map.get(key).getFirst()) {  // Если это не сам файл, а архив
                                ZipInputStream zis = new ZipInputStream(is);
                                ZipEntry zipFileName = zis.getNextEntry();
                                if (zipFileName != null) { // в архиве есть файл
                                    // дальше работаем с первым файлом архива вместо самого архива
                                    is = zis;
                                } else {
                                    break;
                                }
                            }
                            logger.info("Импорт данных справочника из файла «" + fileName + "».");

                            // Обращение к скрипту
                            Map<String, Object> additionalParameters = new HashMap<String, Object>();
                            additionalParameters.put("inputStream", is);
                            refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT, logger, additionalParameters);
                            refBookImportCount++;
                        } catch (Exception e) {
                            //// Ошибка импорта отдельного справочника — откатываются изменения только по нему, импорт продолжается
                            withError = true;
                            String errorMsg;
                            if (e != null && e.getLocalizedMessage() != null) {
                                errorMsg = e.getLocalizedMessage()+".";
                            } else {
                                errorMsg = "";
                            }

                            errorMsg = "Не удалось выполнить импорт данных справочника (id = " + refBookId + ") из файла «"
                                    + fileName + "». " + errorMsg;

                            // Журнал аудита
                            auditService.add(FormDataEvent.IMPORT, userInfo, userInfo.getUser().getDepartmentId(),
                                    null, null, null, null, errorMsg);

                            logger.error(errorMsg);
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                        break;
                    }
                }
            }
            String msg = "Произведен импорт данных справочников из «" + refBookDirectory + "»" +
                    (withError ? " с ошибками." : " без ошибок.") + " Импортировано файлов: " + refBookImportCount + ".";

            if (refBookImportCount == 0) {
                msg = "Импорт не выполнен, файлов с данными справочников в папке «" + refBookDirectory + "» не найдено.";
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