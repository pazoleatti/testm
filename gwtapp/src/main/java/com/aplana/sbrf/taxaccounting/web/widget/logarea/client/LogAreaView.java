package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.widget.log.LogEntriesView;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Map;

public class LogAreaView extends ViewWithUiHandlers<LogAreaUiHandlers> implements
		LogAreaPresenter.MyView {
	
	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<span>Список ошибок (всего: {0}; фатальных: {1})</span>")
		SafeHtml title(int full, int error);
	} 
	
	private static Templates templates = GWT.create(Templates.class);

	interface Binder extends UiBinder<Widget, LogAreaView> {
	}

	@Inject
	public LogAreaView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}
	
	@UiField
	LogEntriesView logEntries;
	
	@UiField
	HTML title;

    @UiField
    FormPanel formPanel;

    @UiField
    LinkAnchor printButton;

    @Override
    public LogEntriesView getLogEntriesView() {
        return logEntries;
    }

    @Override
    public void setLogEntriesCount(Map<LogLevel, Integer> map) {
        if (map == null) {
            title.setHTML(templates.title(0, 0));
            return;
        }
        title.setHTML(templates.title(map.get(LogLevel.ERROR) + map.get(LogLevel.WARNING) + map.get(LogLevel.INFO),
                map.get(LogLevel.ERROR)));
    }

    @Override
    public void setPrintLink(String link) {
        printButton.setHref(link);
        printButton.setVisible(link != null && !link.isEmpty());
    }

    @UiHandler("hideButton")
	void hide(ClickEvent event){
		getUiHandlers().clean();
		getUiHandlers().hide();
	}
}
