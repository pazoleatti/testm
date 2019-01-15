package com.aplana.sbrf.taxaccounting.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Пара КПП/ОКТМО
 */
@Getter
@Setter
@EqualsAndHashCode
public class KppOktmoPair {
    String kpp;
    String oktmo;
}
