package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPrevOss;

/**
 * Сервис для работы с "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
 */
public interface RaschsvUplPrevOssService {

    /**
     * Сохранение "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     * @param raschsvUplPrevOss
     * @return
     */
    Long insertUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss);
}
