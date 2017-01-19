package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif22425;

/**
 * DAO-интерфейс для работы с СвПримТариф2.2.425
 */
public interface RaschsvSvPrimTarif22425Dao {

    /**
     * Сохранение СвПримТариф2.2.425
     * @param raschsvSvPrimTarif22425
     * @return
     */
    Long insertRaschsvSvPrimTarif22425(RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425);

    /**
     * Выборка из СвПримТариф2.2.425
     * @param obyazPlatSvId
     * @return
     */
    RaschsvSvPrimTarif22425 findRaschsvSvPrimTarif22425(Long obyazPlatSvId);
}
