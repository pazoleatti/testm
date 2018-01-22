package com.aplana.sbrf.taxaccounting.permissions.logging.units;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.stereotype.Component;

/**
 * Реализация {@link CheckUnit} для проверки является ли форма консолидированной.
 */
@Component
public class ConsolidateDeclarationFormKindCheckUnit extends AbstractDeclarationFormKindCheckUnit {

    @Override
    public boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, String operationName) {
        return checkType(logger, declarationData, operationName, DeclarationFormKind.CONSOLIDATED);
    }
}
