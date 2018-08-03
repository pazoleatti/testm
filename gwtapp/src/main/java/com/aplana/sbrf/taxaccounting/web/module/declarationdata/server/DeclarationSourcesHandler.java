package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Обработчик получения списка источников/приемников
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class DeclarationSourcesHandler extends AbstractActionHandler<SourcesAction, SourcesResult> {

    @Autowired
    SourceService sourceService;
    @Autowired
    DeclarationDataService declarationDataService;
    @Autowired
    SecurityService securityService;
    @Autowired
    DeclarationDataScriptingService scriptingService;

    public DeclarationSourcesHandler() {
        super(SourcesAction.class);
    }

    @Override
    public SourcesResult execute(SourcesAction action, ExecutionContext executionContext) throws ActionException {
        SourcesResult result = new SourcesResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationId());
            return result;
        }
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        DeclarationData declaration = declarationDataService.get(action.getDeclarationId(), userInfo);
        List<Relation> relationList = new ArrayList<Relation>();

        //Получаем нф-источники
        relationList.addAll(sourceService.getDeclarationSourcesInfo(declaration.getId()));
        relationList.addAll(sourceService.getDeclarationDestinationsInfo(declaration.getId()));
        result.setData(relationList);
        return result;
    }

    @Override
    public void undo(SourcesAction sourcesAction, SourcesResult sourcesResult, ExecutionContext executionContext) throws ActionException {

    }
}
