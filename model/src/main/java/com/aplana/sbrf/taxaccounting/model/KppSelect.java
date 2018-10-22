package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Значение КПП, для использования в select2, т.к. он требует id
 */
@Data
@AllArgsConstructor
public class KppSelect {
    /**
     * rownum
     */
    private int id;
    /**
     * КПП
     */
    private String kpp;
}
