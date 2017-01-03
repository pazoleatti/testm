package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvSum1Tip;

/**
 * DAO-интерфейс для работы с таблицей "Сведения по суммам (тип 1)"
 */
public interface RaschsvSvSum1TipDao {

    /**
     * Сохранение "Сведения по суммам (тип 1)"
     * @param raschsvSvSum1Tip
     * @return
     */
    Long insertRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip);
}
