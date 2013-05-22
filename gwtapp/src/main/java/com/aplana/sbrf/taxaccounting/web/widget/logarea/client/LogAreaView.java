package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.log.LogEntriesView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

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

	@Override
	public void setLogEntries(List<LogEntry> entries) {
		logEntries.setLogEntries(entries);
	}

	@Override
	public void setLogSize(int full, int error, int warn, int info) {
		title.setHTML(templates.title(full, error));
	}

    @Override
    public FormPanel getFormPanel() {
        return formPanel;
    }

    @Override
    public void setFormPanel(FormPanel formPanel) {
        this.formPanel = formPanel;
    }

	@UiHandler("printButton")
	void print(ClickEvent event){
        FormPanel form1 = this.getFormPanel();
        form1.clear();
        DOM.setElementProperty(form1.getElement(), "enctype", "text/plain");
        DOM.setElementProperty(form1.getElement(), "encoding", "text/plain");//for IE8 encoding only
        TextBox textBox = new TextBox();
        textBox.setName("jsonobject");
        textBox.setVisible(false);
        
        textBox.setText(getUiHandlers().print());
        form1.add(textBox);

        form1.submit();
	}

	@UiHandler("hideButton")
	void hide(ClickEvent event){
		getUiHandlers().clean();
		getUiHandlers().hide();
	}
	
}
