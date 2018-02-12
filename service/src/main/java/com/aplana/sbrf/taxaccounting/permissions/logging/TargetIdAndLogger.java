package com.aplana.sbrf.taxaccounting.permissions.logging;

import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.Serializable;

/**
 * Передает данные для расчета прав доступа. Используется в тех случаях, когда при расчете прав доступа требуется
 * логгирование
 */
public class TargetIdAndLogger implements Serializable {

    private Long id;

    private Logger logger;

    public TargetIdAndLogger() {
    }

    public TargetIdAndLogger(Long id, Logger logger) {
        this.id = id;
        this.logger = logger;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
