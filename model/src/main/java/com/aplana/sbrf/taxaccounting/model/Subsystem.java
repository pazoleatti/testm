package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Подсистема системы "Учёт налогов".
 * Содержится в справочнике "Участники информационного обмена" SUBSYSTEM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subsystem {
    private long id;
    private String code;
    private String name;
    private String shortName;

    public Subsystem(long id) {
        this.id = id;
    }
}
