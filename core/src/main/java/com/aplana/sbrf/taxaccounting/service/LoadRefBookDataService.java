package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ImportCounter;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис загрузки ТФ справочников
 *
 * @author Dmitriy Levykin
 */
public interface LoadRefBookDataService {
    /**
     * Загрузка ТФ справочников из ЦАС НСИ (ACCOUNT_PLAN_UPLOAD_DIRECTORY, OKATO_UPLOAD_DIRECTORY, REGION_UPLOAD_DIRECTORY)
     */
    ImportCounter importRefBookNsi(TAUserInfo userInfo, Logger logger);

    /**
     * Загрузка ТФ справочников из Diasoft (DIASOFT_UPLOAD_DIRECTORY)
     */
    ImportCounter importRefBookDiasoft(TAUserInfo userInfo, Logger logger);

    /**
     * Соответствие имени файла ТФ справочников, поставляемых ЦАС НСИ:
     * «Коды ОКАТО»
     * «Коды субъектов Российской Федерации»
     * «План счетов бухгалтерского учета»
     */
    boolean isNSIFile(String name);

    /**
     * Соответствие имени файла ТФ справочников, поставляемых АС Diasoft Custody:
     * «Ценные бумаги»
     * «Эмитенты»
     */
    boolean isDiasoftFile(String name);

    /**
     * TODO Перенести в отдельный сервис
     * Вызов события FormDataEvent.SAVE для скрипта справочника
     * http://jira.aplana.com/browse/SBRFACCTAX-6780
     *
     * @param saveRecords новые значения для проверки по БЛ
     * @param validDateFrom действует с
     * @param validDateTo действует по
     * @param isNewRecords признак новой записи
     */
    void saveRefBookRecords(long refBookId, List<Map<String, RefBookValue>> saveRecords, Date validDateFrom,
                                   Date validDateTo, boolean isNewRecords, TAUserInfo userInfo, Logger logger);
}
