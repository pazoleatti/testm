package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Andrey Drunk
 */
@NoArgsConstructor
@Setter
@Getter
@ToString(of = {"code"})
public class Country extends IdentityObject<Long> {

    private String code;

    private String code2;

    private String name;

    private String fullname;

    public Country(Long id, String code) {
        this.id = id;
        this.code = code;
    }
}
