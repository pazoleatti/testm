package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnm;

import java.util.List;

/**
 * DAO-интерфейс для работы с РасчСВ_ОСС.ВНМ
 */
public interface RaschsvOssVnmDao {

    /**
     * Сохранение РасчСВ_ОСС.ВНМ
     * @param raschsvOssVnm
     * @return
     */
    Long insertRaschsvOssVnm(RaschsvOssVnm raschsvOssVnm);

    /**
     * Выборка из РасчСВ_ОСС.ВНМ
     * @param obyazPlatSvId
     * @return
     */
    RaschsvOssVnm findOssVnm(Long obyazPlatSvId);
}
