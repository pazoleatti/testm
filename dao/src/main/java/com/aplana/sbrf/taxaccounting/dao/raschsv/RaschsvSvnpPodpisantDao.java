package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;

/**
 * DAO-интерфейс для работы с таблицей "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
 */
public interface RaschsvSvnpPodpisantDao {

    /**
     * Сохранение "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
     * @param raschsvSvnpPodpisant
     * @return
     */
    Long insertRaschsvSvnpPodpisant(RaschsvSvnpPodpisant raschsvSvnpPodpisant);
}
