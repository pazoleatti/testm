package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnm;

/**
 * DAO-интерфейс для работы с таблицей "Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
 */
public interface RaschsvOssVnmDao {

    /**
     * Сохранение "Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     * @param raschsvOssVnm
     * @return
     */
    Long insertRaschsvOssVnm(RaschsvOssVnm raschsvOssVnm);
}
