package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogStrahLic;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVypl;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVyplDop;

import java.util.Collection;

/**
 * DAO для работы с "Сводные сведения о выплатах"
 */
public interface RaschsvItogVyplDao {

    /**
     * Добавляет сводные показатели
     */
    Long insertItogStrahLic(RaschsvItogStrahLic raschsvItogStrahLic);

    /**
     * Добавляет выплату
     */
    int[] insertItogVypl(Collection<RaschsvItogVypl> raschsvItogVypls);

    /**
     * Добавляет доп. выплату
     */
    int[] insertItogVyplDop(Collection<RaschsvItogVyplDop> raschsvItogVyplDops);
}
