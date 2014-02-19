package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Справочники для которых уникальности записи определяется комбинацией значений атрибутов
 * @author dloshkarev
 */
public enum UniqueAttributeCombination {
    /** Параметры налоговых льгот */
    TAX_EXEMPTION_PARAMS(7L, Arrays.asList(18L, 19L)),
    /** Классификатор расходов Сбербанка России для целей налогового учёта */
    SBRF_COSTS_CLASSIFIER(27L, Arrays.asList(130L, 133L, 134L)),
    /** Классификатор доходов Сбербанка России для целей налогового учёта */
    SBRF_INCOME_CLASSIFIER(28L, Arrays.asList(140L, 143L, 144L)),
    /** Классификатор соответствия счетов бухгалтерского учёта кодам налогового учёта */
    SBRF_ACCORD_CLASSIFIER(29L, Arrays.asList(150L, 151L, 152L, 153L, 154L)),
    /** Ставки транспортного налога */
    TRANSPORT_TAX_RATES(41L, Arrays.asList(411L, 412L, 423L, 414L, 415L, 417L, 418L)),
    /** Оборотная ведомость (Форма 0409101-СБ) */
    TURNOVER_STATEMENT(50L, Arrays.asList(501L, 502L)),
    /** Отчет о прибылях и убытках (Форма 0409102-СБ) */
    PROFITS_AND_LOSSES_REPORT(52L, Arrays.asList(520L, 521L));

    /** Идентификатор справочника */
    private Long refBookId;
    /** Список идентификаторов атрибутов, совокупность значений которых определяет уникальность записи справочника */
    private List<Long> attributeIds;

    private static List<Long> refBookIds;

    private UniqueAttributeCombination(Long refBookId, List<Long> attributeIds) {
        this.refBookId = refBookId;
        this.attributeIds = attributeIds;
    }

    public Long getRefBookId() {
        return refBookId;
    }

    public List<Long> getAttributeIds() {
        return attributeIds;
    }

    public static List<Long> getRefBookIds() {
        if (refBookIds == null) {
            refBookIds = new ArrayList<Long>();
            for (UniqueAttributeCombination item : values()) {
                refBookIds.add(item.getRefBookId());
            }
        }
        return refBookIds;
    }

    public static UniqueAttributeCombination getByRefBookId(Long refBookId) {
        for (UniqueAttributeCombination item : values()) {
            if (item.getRefBookId().equals(refBookId)) {
                return item;
            }
        }
        return null;
    }
}
