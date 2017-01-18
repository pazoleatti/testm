package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPrevOss;

/**
 * DAO-интерфейс для работы с УплПревОСС
 */
public interface RaschsvUplPrevOssDao {

    /**
     * Сохранение УплПревОСС
     * @param raschsvUplPrevOss
     * @return
     */
    Long insertUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss);

    /**
     * Выборка из УплПревОСС
     * @param obyazPlatSvId
     * @return
     */
    RaschsvUplPrevOss findUplPrevOss(Long obyazPlatSvId);
}
