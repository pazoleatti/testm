package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.List;
import java.util.Map;

public interface RefBookExternalService {
	/**
	 * Ворк эраунд
	 * http://jira.aplana.com/browse/SBRFACCTAX-3841
	 *
	 * Это должно выполняться асинхронно шедуллером.
	 *
	 * Реализация временная. Перебирает все папки в директории и грузит в ней все файлы
	 */
	public void importRefBook(TAUserInfo userInfo, Logger logger);

    /**
     * Вызов события FormDataEvent.CHECK для скрипта справочника
     * http://jira.aplana.com/browse/SBRFACCTAX-6780
     *
     * @param checkRecords новые значения для проверки по БЛ
     */
	public void checkRefBook(long refBookId, List<Map<String, RefBookValue>> checkRecords, TAUserInfo userInfo, Logger logger);
}
