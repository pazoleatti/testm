package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Пара КПП/ОКТМО
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KppOktmoPair implements Serializable {
    private String kpp;
    private String oktmo;
}
