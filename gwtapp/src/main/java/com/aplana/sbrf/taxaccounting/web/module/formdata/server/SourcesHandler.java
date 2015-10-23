package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SourcesResult;
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
public class SourcesHandler extends AbstractActionHandler<SourcesAction, SourcesResult> {

    @Autowired
    SourceService sourceService;

    @Autowired
    FormDataScriptingService scriptingService;

    @Autowired
    SecurityService securityService;

    public SourcesHandler() {
        super(SourcesAction.class);
    }

    @Override
    public SourcesResult execute(SourcesAction action, ExecutionContext executionContext) throws ActionException {
        FormData formData = action.getFormData();
        SourcesResult result = new SourcesResult();
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        List<Relation> relationList = new ArrayList<Relation>();

        //Получаем нф-источники
        relationList.addAll(sourceService.getSourcesInfo(formData, true, false, null, userInfo, logger));
        //Получаем нф-приемники
        relationList.addAll(sourceService.getDestinationsInfo(formData, true, false, null, userInfo, logger));
        //Получаем декларации-приемники
        relationList.addAll(sourceService.getDeclarationDestinationsInfo(formData, true, false, null, userInfo, logger));
        result.setData(relationList);
        return result;
    }

    @Override
    public void undo(SourcesAction sourcesAction, SourcesResult sourcesResult, ExecutionContext executionContext) throws ActionException {

    }
}
