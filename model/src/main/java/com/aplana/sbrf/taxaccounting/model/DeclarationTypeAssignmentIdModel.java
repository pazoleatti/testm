package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Модель Назначения налоговых форм подразделениям в упрощенном виде, содержащем только идентификаторы
 */
public class DeclarationTypeAssignmentIdModel implements Serializable {
    /**
     * Id назначения
     */
    private Long id;

    /**
     * Id вида формы
     */
    private Integer declarationTypeId;

    /**
     * Id подразделения
     */
    private Integer departmentId;

    /**
     * Id исполнителей
     */
    private List<Integer> performerIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(Integer declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public List<Integer> getPerformerIds() {
        return performerIds;
    }

    public void setPerformerIds(List<Integer> performerIds) {
        this.performerIds = performerIds;
    }
}
