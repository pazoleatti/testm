package com.aplana.sbrf.taxaccounting.permissions.logging;

import com.aplana.sbrf.taxaccounting.permissions.logging.impl.ConsolidateLoggingPermissionChecker;
import com.aplana.sbrf.taxaccounting.permissions.logging.impl.IdentifyLoggingPermissionChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Фабрика реализаций интерфейса {@link LoggingPermissionChecker}
 */
@Component
public class LoggingPermissionCheckerFactory {

    @Autowired
    private IdentifyLoggingPermissionChecker identifyLoggingPermissionChecker;

    @Autowired
    private ConsolidateLoggingPermissionChecker consolidateLoggingPermissionChecker;

    public LoggingPermissionChecker getLoggingPermissionChecker(LoggingPreauthorizeType type) {
        switch (type) {
            case IDENTIFY: return identifyLoggingPermissionChecker;
            case CONSOLIDATE: return consolidateLoggingPermissionChecker;
            default: return null;
        }
    }
}
