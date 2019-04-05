package com.aplana.sbrf.taxaccounting.model;

import lombok.Data;

/**
 * Подсистема системы "Учёт налогов".
 * Содержится в справочнике "Участники информационного обмена" SUBSYSTEM.
 */
@Data
public class Subsystem {
    private long id;
    private String code;
    private String name;
    private String shortName;
}
