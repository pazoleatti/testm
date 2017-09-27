package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.SecuredEntity;

/**
 * Интерфейс-метка для дао, работающих с защищенными сущностями
 * @author dloshkarev
 */
public interface PermissionDao {
    /**
     * Метод для получения защищенной сущности по id
     */
    SecuredEntity getSecuredEntity(long id);
}
