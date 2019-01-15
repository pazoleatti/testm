package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Пара КПП/ОКТМО, используемая в форме создания отчетности
 */
@Getter
@Setter
public class ReportFormCreationKppOktmoPair extends KppOktmoPair {
    /**
     * Ид элемента для select2
     */
    Long id;
    /**
     * Обозначение актуальности
     */
    String relevance;
}
