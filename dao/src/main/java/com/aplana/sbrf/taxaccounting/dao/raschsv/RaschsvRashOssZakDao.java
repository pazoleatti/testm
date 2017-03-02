package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;

/**
 * DAO-интерфейс для работы с РасхОССЗак
 */
public interface RaschsvRashOssZakDao {

    /**
     * Сохранение РасхОССЗак
     * @param raschsvRashOssZak
     * @return
     */
    Long insertRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak);

    /**
     * Выборка из РасхОССЗак
     * @param declarationDataId
     * @return
     */
    RaschsvRashOssZak findRaschsvRashOssZak(Long declarationDataId);
}
