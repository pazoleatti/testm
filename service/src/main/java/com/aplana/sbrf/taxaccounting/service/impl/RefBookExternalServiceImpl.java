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
import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;

@Service
@Transactional
public class RefBookExternalServiceImpl implements RefBookExternalService {
	
	private Log log = LogFactory.getLog(getClass());
	 

	@Autowired
	private URL refBookDirectory;

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
		BufferedReader reader = null;
		if (log.isDebugEnabled()){
			log.debug("RefBook dir: " + String.valueOf(refBookDirectory));
		}
		try {
			System.out.println();
			URLConnection conn = refBookDirectory.openConnection();
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				URL fileUrl = new URL(String.valueOf(refBookDirectory) + "/"
						+ line);
				if (log.isDebugEnabled()){
					log.debug("RefBook file: " + String.valueOf(fileUrl));
				}
				InputStream is = null;
				try {
					URLConnection fileConn = fileUrl.openConnection();
					is = new BufferedInputStream(fileConn.getInputStream());
					importRefBook(userInfo, logger,
						3l /* TODO: Пока только окато */, is);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		} catch (IOException e) {
			throw new ServiceException(
					"Неудалось выполнить импорт справочников", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

}
