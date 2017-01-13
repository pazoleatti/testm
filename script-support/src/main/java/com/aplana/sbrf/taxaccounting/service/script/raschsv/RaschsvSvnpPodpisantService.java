package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис для работы с "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
 */
@ScriptExposed
public interface RaschsvSvnpPodpisantService {

    /**
     * Сохранение "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
     * @param raschsvSvnpPodpisant
     * @return
     */
    Long insertRaschsvSvnpPodpisant(RaschsvSvnpPodpisant raschsvSvnpPodpisant);
}
