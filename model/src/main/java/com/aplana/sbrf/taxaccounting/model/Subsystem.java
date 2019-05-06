package com.aplana.sbrf.taxaccounting.model;

import lombok.Builder;
import lombok.Data;

/**
 * Подсистема системы "Учёт налогов".
 * Содержится в справочнике "Участники информационного обмена" SUBSYSTEM.
 */
@Data
@Builder
public class Subsystem {
    private long id;
    private String code;
    private String name;
    private String shortName;
}
