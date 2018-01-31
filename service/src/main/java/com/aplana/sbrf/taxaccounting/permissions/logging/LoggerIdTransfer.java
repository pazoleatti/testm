package com.aplana.sbrf.taxaccounting.permissions.logging;

import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.Serializable;

/**
 * Инкапсулирует данные необходимые для записи в {@link com.aplana.sbrf.taxaccounting.model.log.Logger}
 * и идентификатор налоговой формы, используется при авторизации
 */
public class LoggerIdTransfer implements Serializable, SecuredEntity {

    private Long declarationDataId;

    private Logger logger;

    public LoggerIdTransfer() {
    }

    public LoggerIdTransfer(Long declarationDataId, Logger logger) {
        this.declarationDataId = declarationDataId;
        this.logger = logger;
    }

    @Override
    public long getPermissions() {
        return 0;
    }

    @Override
    public void setPermissions(long permissions) {

    }

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
