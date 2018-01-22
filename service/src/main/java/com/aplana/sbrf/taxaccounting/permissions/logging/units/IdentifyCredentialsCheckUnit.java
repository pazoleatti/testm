package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * Реализация {@link CheckUnit} для проверки прав доступа для идентификации налоговой формы.
 */
@Component
public class IdentifyCredentialsCheckUnit extends AbstractCredentialsCheckUnit {

    @Override
    public boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, String operationName) {
        TAUser taUser = userInfo.getUser();

        boolean canView = canView(userInfo, declarationData);

        boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP,
                TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.F_ROLE_OPER);

        if (!canView || !hasRoles) {
            createErrorMsg(logger, declarationData, operationName);
            return false;
        }

        return true;
    }

}
