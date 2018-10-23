package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Andrey Drunk
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"code"})
public class TaxpayerStatus extends RefBookSimple<Long> {

    private String code;

    private String name;

    public TaxpayerStatus(Long id, String code) {
        this.id = id;
        this.code = code;
    }

}
