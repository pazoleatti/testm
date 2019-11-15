package com.aplana.sbrf.taxaccounting.model.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Фильтр по подразделению
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentFilter {
    // значения для поиска по имени
    private String name;
    // ид периода. Подразделения будут фильтроваться по наличию открытых в них данного периода
    private Integer reportPeriodId;
    // фильтровать по типу "тербанк"
    private boolean onlyTB;
    // ид типа формы, на которое подразделение имеет назначение
    private Integer assignedToDeclarationTypeId;
    // набор ид подразделений
    private List<Integer> ids;
    // код подразделения
    private Long code;
}
