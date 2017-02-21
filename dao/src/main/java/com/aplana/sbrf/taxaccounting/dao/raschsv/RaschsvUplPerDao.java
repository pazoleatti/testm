package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPer;

import java.util.List;

/**
 * DAO-интерфейс для работы с таблицей "УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО"
 */
public interface RaschsvUplPerDao {

    /**
     * Сохранение "УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО"
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
