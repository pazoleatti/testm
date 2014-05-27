package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Date;
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
     * Загрузка справочников из папки Diasoft Custody
     */
    public void importRefBookDiasoft(TAUserInfo userInfo, Logger logger);

    /**
     * Вызов события FormDataEvent.SAVE для скрипта справочника
     * http://jira.aplana.com/browse/SBRFACCTAX-6780
     *
     * @param saveRecords новые значения для проверки по БЛ
     * @param validDateFrom действует с
     * @param validDateTo действует по
     * @param isNewRecords признак новой записи
     */
    public void saveRefBookRecords(long refBookId, List<Map<String, RefBookValue>> saveRecords, Date validDateFrom,
                                   Date validDateTo, boolean isNewRecords, TAUserInfo userInfo, Logger logger);
}
