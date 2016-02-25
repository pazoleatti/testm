package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class DeclarationTemplateSubreportView extends ViewWithUiHandlers<DeclarationTemplateSubreportUiHandlers>
		implements DeclarationTemplateSubreportPresenter.MyView {

    public interface Binder extends UiBinder<Widget, DeclarationTemplateSubreportView> { }

	@UiField
	Button upColumn, downColumn, addColumn, removeColumn;

    @UiField
    ListBox subreportListBox;

    @UiField
    Panel attrPanel;

    @UiField
    TextBox nameBox, aliasBox;

    @UiField
    LinkAnchor file;

    @UiField
    FormPanel formPanel;

    @UiField
    FileUpload uploader;

    @UiField
    LinkAnchor downloadFile;
    @UiField
    LinkButton deleteFile;

    List<DeclarationSubreport> subreports;

    private static String actionUrl = "upload/uploadController/pattern/";

    private static String respPattern = "(<pre.*?>|<PRE.*?>)(.+?)(</pre>|</PRE>)(.*)";

    @Inject
	@UiConstructor
	public DeclarationTemplateSubreportView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
        init();
	}

	private void init() {
        formPanel.setAction(GWT.getHostPageBaseURL() + actionUrl);

        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String result = event.getResults().replaceAll(respPattern, "$2");
                JSONObject answer = JSONParser.parseLenient(result).isObject();
                if (answer.get(UuidEnum.UUID.toString()) != null) {
                    subreports.get(subreportListBox.getSelectedIndex()).setBlobDataId(answer.get(UuidEnum.UUID.toString()).isString().stringValue());
                    setUniqueParameters();
                }
                String uuid = null;
                boolean isErrors = false;
                if (answer.get(UuidEnum.SUCCESS_UUID.toString()) != null) {
                    uuid = answer.get(UuidEnum.SUCCESS_UUID.toString()).isString().stringValue();
                } else if (answer.get(UuidEnum.ERROR_UUID.toString()) != null) {
                    uuid = answer.get(UuidEnum.ERROR_UUID.toString()).isString().stringValue();
                    isErrors = true;
                }
                getUiHandlers().onEndLoad(new EndLoadFileEvent(uuid, isErrors));
                formPanel.reset();
            }
        });

        uploader.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                getUiHandlers().onStartLoad(new StartLoadFileEvent(uploader.getFilename()));
                formPanel.submit();
            }
        });
    }

    @UiHandler("file")
    void onUploadClick(ClickEvent event) {
        uploader.getElement().<InputElement>cast().click();
    }

    @UiHandler("downloadFile")
    void onDownloadClick(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().downloadFile();
        }
    }

    @UiHandler("deleteFile")
    void onDeleteFile(ClickEvent event) {
        subreports.get(subreportListBox.getSelectedIndex()).setBlobDataId(null);
        downloadFile.setEnabled(false);
        deleteFile.setEnabled(false);
    }

    @UiHandler("nameBox")
    public void onNameTextBoxKeyPressed(KeyUpEvent event){
        changeNameColumn();
    }
    @UiHandler("nameBox")
    public void onNameTextBoxClicked(ClickEvent event){
        changeNameColumn();
    }
    private void changeNameColumn() {
        int index = subreportListBox.getSelectedIndex();
        DeclarationSubreport column = subreports.get(index);
        column.setName(nameBox.getValue());
        setColumnList();
        subreportListBox.setSelectedIndex(index);
    }

    @UiHandler("aliasBox")
    public void onAliasTextBoxKeyPressed(KeyUpEvent event){
        changeAliasColumn();
    }
    @UiHandler("aliasBox")
    public void onAliasTextBoxClicked(ClickEvent event){
        changeAliasColumn();
    }
    private void changeAliasColumn() {
        int index = subreportListBox.getSelectedIndex();
        DeclarationSubreport column = subreports.get(index);
        column.setAlias(aliasBox.getValue());
    }

    @Override
    public void setDeclarationTemplate(DeclarationTemplateExt declaration) {
        subreports = declaration.getDeclarationTemplate().getSubreports();
        if (subreportListBox.getSelectedIndex() >= 0) {
            setupColumns(subreportListBox.getSelectedIndex());
        } else {
            setupColumns(0);
        }
    }

    private void setupColumns(int index) {
        setColumnList();
        setAttributesPanel();
        if (subreports != null && !subreports.isEmpty()) {
            setSubreportAttributeEditor(index);
            subreportListBox.setSelectedIndex(index);
            setUniqueParameters();
        }
    }

    private void setAttributesPanel() {
        attrPanel.setVisible(subreports != null && !subreports.isEmpty());
    }

    private void setColumnList() {
        if (subreports != null) {
            subreportListBox.clear();
            for (DeclarationSubreport subreport : subreports) {
                if (subreport.getOrder() < 10) {
                    subreportListBox.addItem("0" + subreport.getOrder() + " " + subreport.getName());
                } else {
                    subreportListBox.addItem(subreport.getOrder() + " " + subreport.getName());
                }
            }
        }
    }

    private void setSubreportAttributeEditor(int index) {
        flush();
    }

    @Override
    public final void flush() {
    }

    @UiHandler("subreportListBox")
    public void onSelectSubreport(ChangeEvent event){
        setSubreportAttributeEditor(subreportListBox.getSelectedIndex());
        setUniqueParameters();
    }

    private void setUniqueParameters() {
        DeclarationSubreport subreport = subreports.get(subreportListBox.getSelectedIndex());
        nameBox.setValue(subreport.getName());
        aliasBox.setValue(subreport.getAlias());
        if (subreport.getBlobDataId() != null) {
            downloadFile.setEnabled(true);
            deleteFile.setEnabled(true);
        } else {
            downloadFile.setEnabled(false);
            deleteFile.setEnabled(false);
        }
    }

    @UiHandler("upColumn")
    public void onUpColumn(ClickEvent event){
        int ind = subreportListBox.getSelectedIndex();
        DeclarationSubreport subreport = subreports.get(ind);
        flush();

        if (subreport != null) {
            if (ind > 0) {
                DeclarationSubreport exchange = subreports.get(ind - 1);
                exchange.setOrder(ind + 1);
                subreport.setOrder(ind);
                subreports.set(ind - 1, subreport);
                subreports.set(ind, exchange);
                setColumnList();
                subreportListBox.setSelectedIndex(ind - 1);
            }
        }
    }

    @UiHandler("downColumn")
    public void onDownColumn(ClickEvent event){
        int ind = subreportListBox.getSelectedIndex();
        DeclarationSubreport subreport = subreports.get(ind);
        flush();

        if (subreport != null) {
            if (ind < subreports.size() - 1) {
                DeclarationSubreport exchange = subreports.get(ind + 1);
                exchange.setOrder(ind + 1);
                subreport.setOrder(ind + 2);
                subreports.set(ind + 1, subreport);
                subreports.set(ind, exchange);
                setColumnList();
                subreportListBox.setSelectedIndex(ind + 1);
            }
        }
    }

    @UiHandler("addColumn")
    public void onAddColumn(ClickEvent event){
        DeclarationSubreport newSubreport = new DeclarationSubreport();
        newSubreport.setName("Новый отчет");
        newSubreport.setAlias("псевдоним");
        newSubreport.setOrder(subreports.size() + 1);
        newSubreport.setAlias("псевдоним");
        getUiHandlers().addSubreport(newSubreport);
        setupColumns(subreports.size() - 1);
    }

    @UiHandler("removeColumn")
    public void onRemoveColumn(ClickEvent event){
        int index = subreportListBox.getSelectedIndex();
        if (index < 0)
            return;

        getUiHandlers().removeSubreport(subreports.get(index));

        for (int i = index; i < subreports.size(); i++) {
            subreports.get(i).setOrder(subreports.get(i).getOrder() - 1);
        }

        if (index > 0) {
            setupColumns(index-1);
        } else {
            setupColumns(0);
        }
    }


    @Override
    public DeclarationSubreport getSelectedSubreport() {
        int ind = subreportListBox.getSelectedIndex();
        DeclarationSubreport subreport = subreports.get(ind);
        return subreport;
    }
}
