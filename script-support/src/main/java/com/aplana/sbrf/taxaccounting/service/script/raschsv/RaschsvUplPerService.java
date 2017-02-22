package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPer;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы с "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
 */
@ScriptExposed
public interface RaschsvUplPerService {

    /**
     * Сохранение "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
     * @param raschsvUplPerList - перечень "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
     * @return
     */
    Integer insertUplPer(List<RaschsvUplPer> raschsvUplPerList);

    /**
     * Выборка "УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО"
     * @return
     */
    List<RaschsvUplPer> findUplPer(Long declarationDataId);
}
