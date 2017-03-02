package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif13422;

/**
 * DAO-интерфейс для работы с СвПримТариф1.3.422
 */
public interface RaschsvSvPrimTarif13422Dao {

    /**
     * Сохранение СвПримТариф1.3.422
     * @param raschsvSvPrimTarif13422
     * @return
     */
    Long insertRaschsvSvPrimTarif13422(RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422);

    /**
     * Выборка из СвПримТариф1.3.422
     * @param declarationDataId
     * @return
     */
    RaschsvSvPrimTarif13422 findRaschsvSvPrimTarif13422(Long declarationDataId);
}
