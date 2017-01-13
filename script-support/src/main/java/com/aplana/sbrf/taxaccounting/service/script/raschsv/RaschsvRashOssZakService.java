package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
 */
@ScriptExposed
public interface RaschsvRashOssZakService {

    /**
     * Сохранение "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
     * @param raschsvRashOssZak
     * @return
     */
    Long insertRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak);
}
