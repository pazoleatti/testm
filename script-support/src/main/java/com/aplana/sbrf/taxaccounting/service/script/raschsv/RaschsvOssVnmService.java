package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnm;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с РасчСВ_ОСС.ВНМ
 */
@ScriptExposed
public interface RaschsvOssVnmService {

    /**
     * Сохранение РасчСВ_ОСС.ВНМ
     * @param raschsvOssVnm
     * @return
     */
    Long insertRaschsvOssVnm(RaschsvOssVnm raschsvOssVnm);

    /**
     * Выборка из РасчСВ_ОСС.ВНМ
     * @param declarationDataId
     * @return
     */
    RaschsvOssVnm findOssVnm(Long declarationDataId);
}
