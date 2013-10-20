package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import jcifs.smb.SmbFileInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;

@Service
@Transactional
public class RefBookExternalServiceImpl implements RefBookExternalService {

    private Log log = LogFactory.getLog(getClass());

	@Autowired
	private ConfigurationService configurationService;

    @Autowired
    RefBookScriptingService refBookScriptingService;

    @Override
    public void importRefBook(TAUserInfo userInfo, Logger logger,
                              Long refBookId, InputStream is) {
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("inputStream", is);
        refBookScriptingService.executeScript(userInfo, refBookId,
                FormDataEvent.IMPORT, logger, additionalParameters);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошли ошибки в скрипте импорта справочника",
                    logger.getEntries());
        }
    }

    /* (non-Javadoc)
     * @see com.aplana.sbrf.taxaccounting.service.RefBookExternalService#importRefBook(com.aplana.sbrf.taxaccounting.model.TAUserInfo, com.aplana.sbrf.taxaccounting.model.log.Logger)
     */
    @Override
    public void importRefBook(TAUserInfo userInfo, Logger logger) {

        // регулярка файла/архива  - true/false  - id
        Map<String, Pair<Boolean, Long>> map = new HashMap<String, Pair<Boolean, Long>>();
        // файл для загр. спр. "Коды ОКАТО"
        map.put("payments.OKATO..*", new Pair<Boolean, Long>(true, 3L));
        // архив для загр. спр. "Коды ОКАТО"
        map.put("OKA.*", new Pair<Boolean, Long>(false, 3L));
        // файл для загр. спр. "Организации-участники контролируемых сделок"
        map.put("organization.xls", new Pair<Boolean, Long>(true, 9L));

		//TODO добавить проверку ЭЦП (Marat Fayzullin 2013-10-19)
		Map<ConfigurationParam, String> params = configurationService.getAllConfig(userInfo);
		String refBookDirectory = params.get(ConfigurationParam.REF_BOOK_DIRECTORY);

        BufferedReader reader = null;
        if (log.isDebugEnabled()) {
            log.debug("RefBook dir: " + refBookDirectory);
        }
		//TODO реализовать работу по smb протоколу (Marat Fayzullin 2013-10-19)
        /*try {
            System.out.println();
            URLConnection conn = refBookDirectory.openConnection();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Ищем в мапе регуляку подходящую для полученного имени файла
                for (String key : map.keySet()) {
                    if (line.matches(key)) { // Нашли в мапе соответствие
                        InputStream is = null;
                        try {
                            URL fileUrl = new URL(String.valueOf(refBookDirectory) + "/" + line);
                            if (log.isDebugEnabled()) {
                                log.debug("RefBook file: " + String.valueOf(fileUrl));
                            }
                            URLConnection fileConn = fileUrl.openConnection();
                            is = new BufferedInputStream(fileConn.getInputStream());

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
                            importRefBook(userInfo, logger, map.get(key).getSecond(), is);

                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                        break;
                    }
                }

            }
        } catch (IOException e) {
            throw new ServiceException(
                    "Неудалось выполнить импорт справочников", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }*/
    }

}