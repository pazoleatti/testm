package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOms;

import java.util.List;

/**
 * DAO-интерфейс для работы с таблицей "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
 */
public interface RaschsvSvOpsOmsDao {

    /**
     * Сохранение "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
     * @param raschsvSvOpsOmsList
     * @return
     */
    Integer insertRaschsvSvOpsOms(List<RaschsvSvOpsOms> raschsvSvOpsOmsList);
}
