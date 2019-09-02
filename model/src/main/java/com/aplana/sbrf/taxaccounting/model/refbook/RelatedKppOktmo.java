package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

/**
 * Учитывать в КПП/ОКТМО
 */
@Getter
@Setter
public class RelatedKppOktmo {
    // КПП
    private String kpp;
    // ОКТМО
    private String oktmo;
}
