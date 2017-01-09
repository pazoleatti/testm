package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;

/**
 * Сервис для работы с "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
 */
public interface RaschsvSvnpPodpisantService {

    /**
     * Сохранение "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
     * @param raschsvSvnpPodpisant
     * @return
     */
    Long insertRaschsvSvnpPodpisant(RaschsvSvnpPodpisant raschsvSvnpPodpisant);
}
