package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSection;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * <b>Удаление выбранных строк в декларации (Раздел 1,2)</b>
 * <p>
 * Created by <i><b>s.molokovskikh</i></b> on 23.10.19.
 */
@Setter
@Getter
public class DeleteSelectedDeclarationRowsAction implements Serializable {

    private static final long serialVersionUID = 1352707488262122817L;
    /**
     * Идентификатор декларации
     */
    private Long declarationDataId;

    /**
     * Раздел декларации
     */
    private DeclarationDataSection section;

    /**
     * Список идентификаторов выделенных строк соответсвующего раздела
     * Для Раздела 1 это будет список ndflPerson.Id
     * Для Раздела 2 это будет список ndflPersonIncome.Id
     */
    private List<Long> sectionIds;

}

