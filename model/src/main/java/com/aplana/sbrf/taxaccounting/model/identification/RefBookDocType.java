package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import lombok.*;

/**
 * Тип документа
 *
 * @author Andrey Drunk
 */
@Getter
@Setter
@NoArgsConstructor
public class RefBookDocType extends IdentityObject<Long> {

    private String name;

    private String code;

    private Integer priority;

    public RefBookDocType(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefBookDocType docType = (RefBookDocType) o;

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
