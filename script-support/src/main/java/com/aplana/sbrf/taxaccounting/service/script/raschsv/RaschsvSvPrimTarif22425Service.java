package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif22425;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с "Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426)"
 */
@ScriptExposed
public interface RaschsvSvPrimTarif22425Service {

    /**
     * Сохранение "Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426)"
     * @param raschsvSvPrimTarif22425
     * @return
     */
    Long insertRaschsvSvPrimTarif22425(RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425);
}
