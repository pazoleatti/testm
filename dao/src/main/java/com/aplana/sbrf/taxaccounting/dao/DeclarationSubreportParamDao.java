package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationSubreportParam;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;

import java.util.List;

/**
 * Dao-объект для работы с {@link com.aplana.sbrf.taxaccounting.model.DeclarationSubreportParam параметрами спец. отчетов}
 * @author lhaziev
 */
public interface DeclarationSubreportParamDao {

    /**
     * Получение списка спец. отчетов
     * @param declarationSubreportId
     * @return
     */
    List<DeclarationSubreportParam> getDeclarationSubreportParams(long declarationSubreportId);

    /**
     * Получение спец отчета по alias
     * @param declarationSubreportId
     * @param alias
     * @return
     */
    DeclarationSubreportParam getSubreportParamByAlias(long declarationSubreportId, String alias);

    /**
     * Обновление набора спец. отчетов
     * @param declarationTemplate
     * @return
     */
    void updateDeclarationSubreports(final DeclarationTemplate declarationTemplate);

    /**
     * Получить спец. отчет по id
     * @param id
     * @return
     */
    DeclarationSubreportParam getSubreportParamById(long id);
}
