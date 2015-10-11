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

        /** Проверяем в скрипте источники-приемники для особенных форм */
        Map<String, Object> params = new HashMap<String, Object>();
        FormSources sources = new FormSources();
        sources.setSourceList(new ArrayList<Relation>());
        sources.setSourcesProcessedByScript(false);
        params.put("sources", sources);
        scriptingService.executeScript(userInfo, formData, FormDataEvent.GET_SOURCES, logger, params);

        if (sources.isSourcesProcessedByScript()) {
            //Скрипт возвращает все необходимые источники-приемники
            if (sources.getSourceList() != null) {
                relationList.addAll(sources.getSourceList());
            }
        } else {
            //Получаем нф-источники
            relationList.addAll(sourceService.getSourcesInfo(formData.getId(), true));
            //Получаем нф-приемники
            relationList.addAll(sourceService.getDestinationsInfo(formData.getId(), true));
            //Получаем декларации-приемники
            relationList.addAll(sourceService.getDeclarationDestinationsInfo(formData.getId(), true));
        }
        result.setData(relationList);
        return result;
    }

    @Override
    public void undo(SourcesAction sourcesAction, SourcesResult sourcesResult, ExecutionContext executionContext) throws ActionException {

    }
}
