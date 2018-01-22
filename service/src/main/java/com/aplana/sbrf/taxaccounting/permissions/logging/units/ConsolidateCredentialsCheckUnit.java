package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.stereotype.Component;

/**
 * Реализация {@link CheckUnit} для проверки прав доступа для консолидации налоговой формы.
 */
@Component
public class ConsolidateCredentialsCheckUnit extends AbstractCredentialsCheckUnit {


    @Override
    public boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, String operationName) {
        TAUser taUser = userInfo.getUser();

        boolean canView = canView(userInfo, declarationData);

        boolean hasRoles = taUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);

        if (!canView || !hasRoles) {
            createErrorMsg(logger, declarationData, operationName);
            return false;
        }
        return true;
    }
}
