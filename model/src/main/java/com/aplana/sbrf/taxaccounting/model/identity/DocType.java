package com.aplana.sbrf.taxaccounting.model.identity;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Тип документа
 * @author Andrey Drunk
 */
public class DocType extends IdentityObject<Long> {

    private String name;

    private String code;

    private Integer priority;

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
    public String toString() {
        return new StringBuilder().append("[").append(id).append(", ").append(code).append("]").toString();
    }
}
