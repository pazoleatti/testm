package com.aplana.sbrf.taxaccounting.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Пара КПП/ОКТМО
 */
@Getter
@Setter
@EqualsAndHashCode
public class KppOktmoPair implements Serializable {
    String kpp;
    String oktmo;
}
