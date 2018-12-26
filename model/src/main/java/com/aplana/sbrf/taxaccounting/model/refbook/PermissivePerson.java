package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.SecuredEntity;

/**
 * Интерфейс ФЛ, необходимый для определения прав доступа
 */
public interface PermissivePerson extends SecuredEntity {

    Long getId();

    Long getRecordId();

    boolean isVip();
}
