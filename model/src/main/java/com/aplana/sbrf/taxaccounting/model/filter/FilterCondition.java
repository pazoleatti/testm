package com.aplana.sbrf.taxaccounting.model.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Условие, применяемое для некоторого выражения
 */
@Getter
@Setter
@NoArgsConstructor
public class FilterCondition {
    public enum OperatorEnum {
        // Заполнено
        FILLED,
        // Не заполнено
        BLANK,
        // Больше
        HIGHER,
        // Меньше
        LOWER,
        // Равно
        EQUAL,
        // Не равно
        UNEQUAL
    }

    public FilterCondition(OperatorEnum operator) {
        this.operator = operator;
    }

    public FilterCondition(OperatorEnum operator, BigDecimal argument2) {
        this.operator = operator;
        this.argument2 = argument2;
    }

    OperatorEnum operator;
    BigDecimal argument2;
}
