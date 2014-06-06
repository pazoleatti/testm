package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormToFormRelation;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Обработчик получения списка источников/приемников
 */
@Service
public class SourcesHandler extends AbstractActionHandler<SourcesAction, SourcesResult> {

    @Autowired
    SourceService sourceService;

    public SourcesHandler() {
        super(SourcesAction.class);
    }

    @Override
    public SourcesResult execute(SourcesAction action, ExecutionContext executionContext) throws ActionException {
        FormData formData = action.getFormData();
        SourcesResult result = new SourcesResult();
        List<FormToFormRelation> relationList = sourceService.getRelations(
                formData.getDepartmentId(),
                formData.getFormType().getId(),
                formData.getKind(),
                formData.getReportPeriodId(),
                formData.getPeriodOrder(),
                action.isShowDestinations(),
                action.isShowSources(),
                action.isShowUncreated()
        );

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
                        // Сотируем по состоянию формы
                        if (!o1.isCreated()){
                            return 1;
                        } else if (!o2.isCreated()){
                            return -1;
                        } else{
                            return o1.getState().getName().compareTo(o2.getState().getName());
                        }
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
