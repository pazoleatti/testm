package com.aplana.sbrf.taxaccounting.service.component;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class MoveToCreateFacade {

    private DeclarationDataScriptingService declarationDataScriptingService;

    @Autowired
    public MoveToCreateFacade(DeclarationDataScriptingService declarationDataScriptingService) {
        this.declarationDataScriptingService = declarationDataScriptingService;
    }

    @PreAuthorize("hasPermission(#declarationData.id, 'com.aplana.sbrf.taxaccounting.model.DeclarationData', T(com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission).RETURN_TO_CREATED)")
    public void cancel(TAUserInfo userInfo, DeclarationData declarationData, Logger logger, Map<String, Object> exchangeParams) {
        declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.MOVE_ACCEPTED_TO_CREATED, logger, exchangeParams);
    }
}
