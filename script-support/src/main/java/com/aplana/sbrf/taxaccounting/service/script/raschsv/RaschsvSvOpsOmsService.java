package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOms;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы с РасчСВ_ОПС_ОМС
 */
@ScriptExposed
public interface RaschsvSvOpsOmsService {

    /**
     * Сохраняет перечень записей РасчСВ_ОПС_ОМС
     * @param raschsvSvOpsOmsList
     * @return
     */
    Integer insertRaschsvSvOpsOms(List<RaschsvSvOpsOms> raschsvSvOpsOmsList);

    /**
     * Выборка из РасчСВ_ОПС_ОМС
     * @param declarationDataId
     * @return
     */
    List<RaschsvSvOpsOms> findSvOpsOms(Long declarationDataId);
}
