package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplFinFb;

/**
 * DAO-интерфейс для работы с таблицей "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
 */
public interface RaschsvVyplFinFbDao {

    /**
     * Сохранение "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
     * @param raschsvVyplFinFb
     * @return
     */
    Long insertRaschsvVyplFinFb(RaschsvVyplFinFb raschsvVyplFinFb);
}
