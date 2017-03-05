package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif71427;

/**
 * DAO-интерфейс для работы с ПравТариф7.1.427
 */
public interface RaschsvPravTarif71427Dao {

    /**
     * Сохранение ПравТариф7.1.427
     * @param raschsvPravTarif71427
     * @return
     */
    Long insertRaschsvPravTarif71427(RaschsvPravTarif71427 raschsvPravTarif71427);

    /**
     * Выборка из ПравТариф7.1.427
     * @param declarationDataId
     * @return
     */
    RaschsvPravTarif71427 findRaschsvPravTarif71427(Long declarationDataId);
}
