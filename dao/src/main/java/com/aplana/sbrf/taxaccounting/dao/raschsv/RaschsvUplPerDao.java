package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPer;

import java.util.List;

/**
 * DAO-интерфейс для работы с таблицей "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
 */
public interface RaschsvUplPerDao {

    /**
     * Сохранение "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
     * @param raschsvUplPerList - перечень "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
     * @return
     */
    Integer insertUplPer(List<RaschsvUplPer> raschsvUplPerList);
}
