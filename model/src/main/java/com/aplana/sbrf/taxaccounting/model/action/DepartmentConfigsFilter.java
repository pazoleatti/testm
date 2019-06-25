package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.json.ISODateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date relevanceDate;
    private String kpp;
    private String oktmo;
    private String taxOrganCode;
}
