package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormToFormRelation;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
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
        List<FormToFormRelation> relationList = new ArrayList<FormToFormRelation>();

        /** Проверяем в скрипте источники-приемники для особенных форм */
        Map<String, Object> params = new HashMap<String, Object>();
        List<FormToFormRelation> sourceList = new ArrayList<FormToFormRelation>();
        Boolean sourcesProcessedByScript = null;
        params.put("sourceList", sourceList);
        params.put("sourcesProcessedByScript", sourcesProcessedByScript);
        scriptingService.executeScript(userInfo, formData, FormDataEvent.GET_SOURCES, logger, params);

        if (sourcesProcessedByScript != null && sourcesProcessedByScript) {
            //Скрипт возвращает все необходимые источники-приемники
            relationList.addAll(sourceList);
        } else {
            //Получаем источники-приемники стандарртными методами ядра
            relationList = sourceService.getRelations(
                    formData.getDepartmentId(),
                    formData.getFormType().getId(),
                    formData.getKind(),
                    formData.getDepartmentReportPeriodId(),
                    formData.getPeriodOrder()
            );
        }

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
                        return o1.getState().getName().compareTo(o2.getState().getName());
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
