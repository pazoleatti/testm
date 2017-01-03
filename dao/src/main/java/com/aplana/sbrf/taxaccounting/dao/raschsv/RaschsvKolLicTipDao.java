package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvKolLicTip;

/**
 * DAO-интерфейс для работы с таблицей "Сведения по количеству физических лиц"
 */
public interface RaschsvKolLicTipDao {

    /**
     * Сохранение "Сведения по количеству физических лиц"
     * @param raschsvKolLicTip
     * @return
     */
    Long insertRaschsvKolLicTip(RaschsvKolLicTip raschsvKolLicTip);
}
