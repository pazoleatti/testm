package com.aplana.sbrf.taxaccounting.model.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Фильтр для поиска/отбора настроек подразделений
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentConfigsFilter implements Serializable {
    private Integer departmentId;
    private Date relevanceDate;
    private String kpp;
    private String oktmo;
    private String taxOrganCode;
}
