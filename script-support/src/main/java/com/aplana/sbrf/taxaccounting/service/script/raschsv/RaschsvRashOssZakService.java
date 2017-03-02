package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с РасхОССЗак
 */
@ScriptExposed
public interface RaschsvRashOssZakService {

    /**
     * Сохранение РасхОССЗак
     * @param raschsvRashOssZak
     * @return
     */
    Long insertRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak);

    /**
     * Выборка из РасхОССЗак
     * @param declarationDataId
     * @return
     */
    RaschsvRashOssZak findRaschsvRashOssZak(Long declarationDataId);
}
