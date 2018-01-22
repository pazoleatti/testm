package com.aplana.sbrf.taxaccounting.permissions.logging.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.permissions.logging.LoggingPermissionChecker;
import com.aplana.sbrf.taxaccounting.permissions.logging.units.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

/**
 * Реализация {@link LoggingPermissionChecker} для операции идентификации.
 */
@Component
public class IdentifyLoggingPermissionChecker implements LoggingPermissionChecker {

    @Autowired
    private CheckUnit primaryDeclarationFormKindCheckUnit;

    @Autowired
    private CheckUnit identifyCredentialsCheckUnit;

    @Autowired
    private CheckUnit activePeriodCheckUnit;

    @Autowired
    private CheckUnit statePrimaryPreparedCheckUnit;

    private final List<CheckUnit> CHECK_UNITS = new LinkedList<>();

    @PostConstruct
    private void init() {
        CHECK_UNITS.add(primaryDeclarationFormKindCheckUnit);
        CHECK_UNITS.add(activePeriodCheckUnit);
        CHECK_UNITS.add(statePrimaryPreparedCheckUnit);
        CHECK_UNITS.add(identifyCredentialsCheckUnit);
    }

    @Override
    public boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData) {
        for (CheckUnit checkUnit : CHECK_UNITS) {
            if (!checkUnit.check(logger, userInfo, declarationData, "Идентификация ФЛ")) {
                return false;
            }
        }
        return true;
    }
}
