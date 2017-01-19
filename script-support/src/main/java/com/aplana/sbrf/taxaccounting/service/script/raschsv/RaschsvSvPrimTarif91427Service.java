package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif91427;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с СвПримТариф9.1.427
 */
@ScriptExposed
public interface RaschsvSvPrimTarif91427Service {

    /**
     * Сохранение СвПримТариф9.1.427
     * @param raschsvSvPrimTarif91427
     * @return
     */
    Long insertRaschsvSvPrimTarif91427(RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427);
}
