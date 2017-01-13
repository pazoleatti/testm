package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPrevOss;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
 */
@ScriptExposed
public interface RaschsvUplPrevOssService {

    /**
     * Сохранение "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     * @param raschsvUplPrevOss
     * @return
     */
    Long insertUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss);
}
