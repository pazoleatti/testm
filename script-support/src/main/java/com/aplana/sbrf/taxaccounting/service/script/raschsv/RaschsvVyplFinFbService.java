package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplFinFb;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с ВыплФинФБ
 */
@ScriptExposed
public interface RaschsvVyplFinFbService {

    /**
     * Сохранение ВыплФинФБ
     * @param raschsvVyplFinFb
     * @return
     */
    Long insertRaschsvVyplFinFb(RaschsvVyplFinFb raschsvVyplFinFb);
}
