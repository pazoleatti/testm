package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPer;

import java.util.List;

/**
 * Сервис для работы с "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
 */
public interface RaschsvUplPerService {

    /**
     * Сохранение "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
     * @param raschsvUplPerList - перечень "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
     * @return
     */
    Integer insertUplPer(List<RaschsvUplPer> raschsvUplPerList);
}
