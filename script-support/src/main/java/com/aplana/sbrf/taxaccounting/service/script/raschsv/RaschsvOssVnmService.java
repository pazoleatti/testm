package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnm;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с "Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
 */
@ScriptExposed
public interface RaschsvOssVnmService {

    /**
     * Сохранение "Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     * @param raschsvOssVnm
     * @return
     */
    Long insertRaschsvOssVnm(RaschsvOssVnm raschsvOssVnm);
}
