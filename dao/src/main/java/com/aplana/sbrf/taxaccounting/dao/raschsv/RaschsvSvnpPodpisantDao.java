package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;

import java.util.List;

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

    /**
     * Выборка из "СвНП и Подписант"
     * @param declarationDataIds
     * @return
     */
    List<RaschsvSvnpPodpisant> findRaschsvSvnpPodpisant(List<Long> declarationDataIds);
}
