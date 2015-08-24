package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
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

    public DeclarationSourcesHandler() {
        super(SourcesAction.class);
    }

    @Override
    public SourcesResult execute(SourcesAction action, ExecutionContext executionContext) throws ActionException {
        SourcesResult result = new SourcesResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        DeclarationData declaration = declarationDataService.get(action.getDeclarationId(), userInfo);
        List<FormToFormRelation> relationList = sourceService.getRelations(declaration);

        Collections.sort(relationList, new Comparator<FormToFormRelation>() {
            @Override
            public int compare(FormToFormRelation o1, FormToFormRelation o2) {
                // вначале сортируем по типу источник/приемник
                if (o1.isSource() ^ (!o2.isSource())){
                    // если тип (источник или приемник) совпали то сортируем по типу формы
                    int type = o1.getFormType().getName().compareTo(o2.getFormType().getName());
                    if (type != 0){
                        return type;
                    } else{
                        // Сотируем дате корректировки
                        if (o1.getCorrectionDate() != null || o2.getCorrectionDate() != null) {
                            if (o1.getCorrectionDate() == null) {
                                return -1;
                            }
                            if (o2.getCorrectionDate() == null) {
                                return 1;
                            }
                            int dateCompare = o1.getCorrectionDate().compareTo(o2.getCorrectionDate());
                            if (dateCompare != 0) {
                                return dateCompare;
                            }
                        }
                        // Сотируем по состоянию формы
                        if (!o1.isCreated()){
                            return 1;
                        } else if (!o2.isCreated()){
                            return -1;
                        }
                        return o1.getState().getTitle().compareTo(o2.getState().getTitle());
                    }
                } else{
                    return o1.isSource() ? 1:-1;
                }
            }
        });
        result.setData(relationList);
        return result;
    }

    @Override
    public void undo(SourcesAction sourcesAction, SourcesResult sourcesResult, ExecutionContext executionContext) throws ActionException {

    }
}
