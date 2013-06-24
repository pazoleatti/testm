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
import com.google.gwt.user.client.Window;
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
    public void getReport(String uuid) {
        Window.open(GWT.getHostPageBaseURL() + "download/downloadBlobController/processLogDownload/" + uuid, "", "");
    }


    @UiHandler("printButton")
	void print(ClickEvent event){
        getUiHandlers().print();
        //Формирование через JSON. Вариант без файлового хранилища
        /*DOM.setElementProperty(formPanel.getElement(), "enctype", "text/plain");
        DOM.setElementProperty(formPanel.getElement(), "encoding", "text/plain");//for IE8 encoding only
        TextBox textBox = new TextBox();
        textBox.setName("jsonobject");
        textBox.setVisible(false);

        textBox.setText(getUiHandlers().print());
        formPanel.add(textBox);

        formPanel.submit();
        formPanel.clear();*/
	}

	@UiHandler("hideButton")
	void hide(ClickEvent event){
		getUiHandlers().clean();
		getUiHandlers().hide();
	}
	
}
