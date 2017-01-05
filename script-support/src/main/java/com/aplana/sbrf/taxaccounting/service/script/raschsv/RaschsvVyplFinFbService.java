package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplFinFb;

/**
 * Сервис для работы с "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
 */
public interface RaschsvVyplFinFbService {

    /**
     * Сохранение "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
     * @param raschsvVyplFinFb
     * @return
     */
    Long insertRaschsvVyplFinFb(RaschsvVyplFinFb raschsvVyplFinFb);
}
