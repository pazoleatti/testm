package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.ImportResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис работы со скриптами справочников. Частная логика и импорт.
 */
public interface RefBookExternalService {
	/**
	 * Загрузка справочников из ЦАС НСИ
	 */
	public ImportResult importRefBookNsi(TAUserInfo userInfo, Logger logger);

    /**
     * Загрузка справочников из Diasoft Custody
     */
    public ImportResult importRefBookDiasoft(TAUserInfo userInfo, Logger logger);

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

    /**
     * Соответствие имени файла ТФ справочников, поставляемых ЦАС НСИ:
     * «Коды ОКАТО»
     * «Коды субъектов Российской Федерации»
     * «План счетов бухгалтерского учета»
     *
     * @param name Имя файла
     * @return
     */
    public boolean isNSIFile(String name);

    /**
     * Соответствие имени файла ТФ справочников, поставляемых АС Diasoft Custody:
     * «Ценные бумаги»
     * «Эмитенты»
     *
     * @param name
     * @return
     */
    public boolean isDiasoftFile(String name);
}
