package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplFinFb;

/**
 * DAO-интерфейс для работы с ВыплФинФБ
 */
public interface RaschsvVyplFinFbDao {

    /**
     * Сохранение ВыплФинФБ
     * @param raschsvVyplFinFb
     * @return
     */
    Long insertRaschsvVyplFinFb(RaschsvVyplFinFb raschsvVyplFinFb);

    /**
     * Выборка из ВыплФинФБ
     * @param declarationDataId
     * @return
     */
    RaschsvVyplFinFb findRaschsvVyplFinFb(Long declarationDataId);
}
