package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.stereotype.Component;

/**
 * Реализация {@link CheckUnit} для проверки является ли форма первичной.
 */
@Component
public class PrimaryDeclarationFormKindCheckUnit extends AbstractDeclarationFormKindCheckUnit {

    @Override
    public boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, String operationName) {
        return checkType(logger, declarationData, operationName, DeclarationFormKind.PRIMARY);
    }
}
