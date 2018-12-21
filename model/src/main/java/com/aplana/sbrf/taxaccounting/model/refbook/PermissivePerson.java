package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissivePerson extends IdentityObject<Long> implements SecuredEntity {
    /**
     * Идентификатор группы версий
     */
    private Long recordId;

    private long permissions;

    private boolean vip;

}
