package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogStrahLic;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVypl;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVyplDop;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Collection;

/**
 * Сервис для работы с "Сводные сведения о выплатах"
 */
@ScriptExposed
public interface RaschsvItogVyplService {

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
