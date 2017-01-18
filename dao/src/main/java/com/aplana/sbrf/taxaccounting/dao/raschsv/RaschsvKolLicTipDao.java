package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvKolLicTip;

/**
 * DAO-интерфейс для работы с КолЛицТип
 */
public interface RaschsvKolLicTipDao {

    /**
     * Сохранение КолЛицТип
     * @param raschsvKolLicTip
     * @return
     */
    Long insertRaschsvKolLicTip(RaschsvKolLicTip raschsvKolLicTip);
}
