package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVypl;

import java.util.List;

/**
 * DAO-интерфейс для работы с таблицей "Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица"
 */
public interface RaschsvSvVyplDao {

    Integer insert(List<RaschsvSvVypl> raschsvSvVyplList);
}
