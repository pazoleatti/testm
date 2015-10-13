package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Обработчик получения списка источников/приемников
 */
@Service
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
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        DeclarationData declaration = declarationDataService.get(action.getDeclarationId(), userInfo);
        List<Relation> relationList = new ArrayList<Relation>();

        /** Проверяем в скрипте источники-приемники для особенных форм */
        Map<String, Object> params = new HashMap<String, Object>();
        FormSources sources = new FormSources();
        sources.setSourceList(new ArrayList<Relation>());
        sources.setSourcesProcessedByScript(false);
        params.put("sources", sources);
        scriptingService.executeScript(userInfo, declaration, FormDataEvent.GET_SOURCES, logger, params);

        if (sources.isSourcesProcessedByScript()) {
            //Скрипт возвращает все необходимые источники-приемники
            if (sources.getSourceList() != null) {
                relationList.addAll(sources.getSourceList());
            }
        } else {
            //Получаем нф-источники
            relationList.addAll(sourceService.getDeclarationSourcesInfo(declaration, true, false, null, userInfo, logger));
        }
        result.setData(relationList);
        return result;
    }

    @Override
    public void undo(SourcesAction sourcesAction, SourcesResult sourcesResult, ExecutionContext executionContext) throws ActionException {

    }
}
