package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPrevOss;

/**
 * DAO-интерфейс для работы с таблицей "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
 */
public interface RaschsvUplPrevOssDao {

    /**
     * Сохранение "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     * @param raschsvUplPrevOss
     * @return
     */
    Long insertUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss);
}
