package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOms;

import java.util.List;

/**
 * DAO-интерфейс для работы с РасчСВ_ОПС_ОМС
 */
public interface RaschsvSvOpsOmsDao {

    /**
     * Сохранение РасчСВ_ОПС_ОМС
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
