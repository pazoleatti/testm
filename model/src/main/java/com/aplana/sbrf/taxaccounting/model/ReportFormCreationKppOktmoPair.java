package com.aplana.sbrf.taxaccounting.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Пара КПП/ОКТМО, используемая в форме создания отчетности
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class ReportFormCreationKppOktmoPair extends KppOktmoPair {
    /**
     * Ид элемента для select2
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Long id;
    /**
     * Обозначение актуальности
     */
    private String relevance;

    public ReportFormCreationKppOktmoPair(String kpp, String oktmo, String relevance) {
        super(kpp, oktmo);
        this.relevance = relevance;
    }
}
