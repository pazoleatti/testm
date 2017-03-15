package com.aplana.sbrf.taxaccounting.service.script.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы с "СвНП и Подписант"
 */
@ScriptExposed
public interface RaschsvSvnpPodpisantService {

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
