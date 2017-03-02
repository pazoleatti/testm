package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif51427;

/**
 * DAO-интерфейс для работы с ПравТариф5.1.427
 */
public interface RaschsvPravTarif51427Dao {

    /**
     * Сохранение ПравТариф5.1.427
     * @param raschsvPravTarif51427
     * @return
     */
    Long insertRaschsvPravTarif51427(RaschsvPravTarif51427 raschsvPravTarif51427);

    /**
     * Выборка из ПравТариф5.1.427
     * @param declarationDataId
     * @return
     */
    RaschsvPravTarif51427 findRaschsvPravTarif51427(Long declarationDataId);
}
