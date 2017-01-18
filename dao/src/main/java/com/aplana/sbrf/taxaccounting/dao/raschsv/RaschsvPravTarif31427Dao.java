package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif31427;

/**
 * DAO-интерфейс для работы с ПравТариф3.1.427
 */
public interface RaschsvPravTarif31427Dao {

    /**
     * Сохранение ПравТариф3.1.427
     * @param raschsvPravTarif31427
     * @return
     */
    Long insertRaschsvPravTarif31427(RaschsvPravTarif31427 raschsvPravTarif31427);

    /**
     * Выборка из ПравТариф3.1.427
     * @param obyazPlatSvId
     * @return
     */
    RaschsvPravTarif31427 findRaschsvPravTarif31427(Long obyazPlatSvId);
}
