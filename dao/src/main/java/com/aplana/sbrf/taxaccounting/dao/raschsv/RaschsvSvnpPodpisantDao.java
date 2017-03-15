package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;

/**
 * DAO-интерфейс для работы с "СвНП и Подписант"
 */
public interface RaschsvSvnpPodpisantDao {

    /**
     * Сохранение "СвНП и Подписант"
     * @param raschsvSvnpPodpisant
     * @return
     */
    Long insertRaschsvSvnpPodpisant(RaschsvSvnpPodpisant raschsvSvnpPodpisant);

    /**
     * Выборка из "СвНП и Подписант"
     * @param declarationDataId
     * @return
     */
    RaschsvSvnpPodpisant findRaschsvSvnpPodpisant(Long declarationDataId);
}
