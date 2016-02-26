package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Dao-объект для работы с {@link com.aplana.sbrf.taxaccounting.model.DeclarationSubreport спец отчетами}
 * @author dsultanbekov
 */
public interface DeclarationSubreportDao {

    /**
     * Получение списка спец. отчетов
     * @param declarationTemplateId
     * @return
     */
    List<DeclarationSubreport> getDeclarationSubreports(int declarationTemplateId);

    /**
     * Получение спец отчета по alias
     * @param declarationTemplateId
     * @param alias
     * @return
     */
    DeclarationSubreport getSubreportByAlias(int declarationTemplateId, String alias);

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
    DeclarationSubreport getSubreportById(int id);
}
