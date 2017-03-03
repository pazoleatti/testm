package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif13422;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с "Сведения об обучающихся, необходимые для применения положений подпункта 1 пункта 3 статьи 422"
 */
@ScriptExposed
public interface RaschsvSvPrimTarif13422Service {

    /**
     * Сохранение "Сведения об обучающихся, необходимые для применения положений подпункта 1 пункта 3 статьи 422"
     * @param raschsvSvPrimTarif13422
     * @return
     */
    Long insertRaschsvSvPrimTarif13422(RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422);

    /**
     * Выборка из СвПримТариф1.3.422
     * @param declarationDataId
     * @return
     */
    RaschsvSvPrimTarif13422 findRaschsvSvPrimTarif13422(Long declarationDataId);
}
