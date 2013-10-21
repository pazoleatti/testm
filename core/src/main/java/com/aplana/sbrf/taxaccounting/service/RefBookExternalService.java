package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.InputStream;

public interface RefBookExternalService {
	
	/**
	 * Импортирует данные справочника
	 * 
	 * @param refBookId код справочника
	 * @return
	 */
	public void importRefBook(TAUserInfo userInfo, Logger logger, Long refBookId, InputStream is) throws ServiceLoggerException;
	
	/**
	 * Ворк эраунд
	 * http://jira.aplana.com/browse/SBRFACCTAX-3841
	 * 
	 * Это должно выполняться асинхронно шедуллером.
	 * 
	 * Реализация временная. Перебирает все папки в директории и грузит в ней все файлы
	 */
	public void importRefBook(TAUserInfo userInfo, Logger logger);
}
