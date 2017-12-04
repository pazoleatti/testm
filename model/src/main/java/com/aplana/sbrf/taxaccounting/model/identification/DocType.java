package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Тип документа
 *
 * @author Andrey Drunk
 */
public class DocType extends IdentityObject<Long> {

    private String name;

    private String code;

    private Integer priority;

    public DocType() {
    }

    public DocType(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocType docType = (DocType) o;

        return code.equals(docType.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[").append(id).append(", ").append(code).append("]").toString();
    }
}
