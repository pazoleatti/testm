package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOms;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы с "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
 */
@ScriptExposed
public interface RaschsvSvOpsOmsService {

    /**
     * Сохраняет перечень записей "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
     * @param raschsvSvOpsOmsList
     * @return
     */
    Integer insertRaschsvSvOpsOms(List<RaschsvSvOpsOms> raschsvSvOpsOmsList);
}
