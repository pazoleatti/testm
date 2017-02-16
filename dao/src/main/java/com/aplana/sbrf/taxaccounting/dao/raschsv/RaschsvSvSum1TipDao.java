package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvSum1Tip;

import java.util.List;

/**
 * DAO-интерфейс для работы с СвСум1Тип
 */
public interface RaschsvSvSum1TipDao {

    /**
     * Сохранение СвСум1Тип
     * @param raschsvSvSum1Tip
     * @return
     */
    Long insertRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip);

    /**
     * Удаление СвСум1Тип по declarationDataId через все связанные таблицы
     * @param declarationDataId
     */
    void deleteRaschsvSvSum1TipByDeclarationDataId(Long declarationDataId);
}
