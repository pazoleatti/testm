package com.aplana.sbrf.taxaccounting.service;

import java.io.InputStream;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

public interface RefBookExternalService {
	
	/**
	 * Импортирует данные справочника
	 * 
	 * @param refBookId код справочника
	 * @return
	 */
	public void importRefBook(TAUserInfo userInfo, Logger logger, Long refBookId, InputStream is);
	
	
	
	/**
	 * Ворк эраунд
	 * http://jira.aplana.com/browse/SBRFACCTAX-3841
	 * 
	 * Это должно выполняться асинхронно шедуллером.
	 * 
	 * Реализация временная. Перебирает все папки в директории и грузит в ней все файлы как ОКАТО
	 */
	public void importRefBook(TAUserInfo userInfo, Logger logger);

}
