package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.TemplateChangesExt;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

/**
 * User: avanteev
 */
public class VersionHistoryPresenter extends PresenterWidget<VersionHistoryView> {

    public interface MyView extends PopupView {
        void fillTemplate(List<TemplateChangesExt> templateChangeses);
    }

    @Inject
    public VersionHistoryPresenter(EventBus eventBus, VersionHistoryView view) {
        super(eventBus, view);
    }

    public void initHistory(List<TemplateChangesExt> changesExtList){
        getView().fillTemplate(changesExtList);
    }

}
