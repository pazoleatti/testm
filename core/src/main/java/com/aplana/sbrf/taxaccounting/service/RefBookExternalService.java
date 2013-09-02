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

}
