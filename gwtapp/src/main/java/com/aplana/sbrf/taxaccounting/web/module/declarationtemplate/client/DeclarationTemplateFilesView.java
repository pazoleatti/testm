package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeclarationTemplateFilesView extends ViewWithUiHandlers<DeclarationTemplateFilesUiHandlers>
		implements DeclarationTemplateFilesPresenter.MyView {

    public interface Binder extends UiBinder<Widget, DeclarationTemplateFilesView> { }

    @UiField
    ListBox fileListBox;

    @UiField
    FormPanel formPanel;

    @UiField
    LinkAnchor file;
    @UiField
    LinkAnchor downloadFile;
    @UiField
    LinkButton deleteFile;

    @UiField
    FileUpload uploader;

    private List<DeclarationTemplateFile> declarationTemplateFiles;

    private static String actionUrl = "upload/uploadController/pattern/";
    private static String respPattern = "(<pre.*?>|<PRE.*?>)(.+?)(</pre>|</PRE>)(.*)";

    @Inject
	@UiConstructor
	public DeclarationTemplateFilesView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
        init();
	}

	private String extractFileName(String fileName) {
        int lastIndex = fileName.lastIndexOf('\\');
        return fileName.substring(lastIndex + 1);
    }

	private void init() {
        formPanel.setAction(GWT.getHostPageBaseURL() + actionUrl);

        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String result = event.getResults().replaceAll(respPattern, "$2");
                JSONObject answer = JSONParser.parseLenient(result).isObject();

                if (answer.get(UuidEnum.UUID.toString()) != null) {
                    DeclarationTemplateFile declarationTemplateFile = new DeclarationTemplateFile();
                    declarationTemplateFile.setBlobDataId(answer.get(UuidEnum.UUID.toString()).isString().stringValue());
                    declarationTemplateFile.setFileName(extractFileName(uploader.getFilename()));

                    addDeclarationTemplateFile(declarationTemplateFile);
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
            getUiHandlers().onTemplateFilesChanged();
        }
    }

    @UiHandler("deleteFile")
    void onDeleteFile(ClickEvent event) {
        declarationTemplateFiles.remove(fileListBox.getSelectedIndex());
        fileListBox.removeItem(fileListBox.getSelectedIndex());
        getUiHandlers().onTemplateFilesChanged();
    }

    @Override
    public void setDeclarationTemplateFiles(List<DeclarationTemplateFile> declarationTemplateFiles) {
        this.declarationTemplateFiles = declarationTemplateFiles;

        fileListBox.clear();
        for (DeclarationTemplateFile templateFile : declarationTemplateFiles) {
            fileListBox.addItem(templateFile.getFileName());
        }
    }

    private void addDeclarationTemplateFile(DeclarationTemplateFile declarationTemplateFile) {
        declarationTemplateFiles.add(declarationTemplateFile);
        fileListBox.addItem(declarationTemplateFile.getFileName());
    }

    @Override
    public DeclarationTemplateFile getDeclarationTemplateFile() {
        if (fileListBox.getSelectedIndex() < 0) {
            return null;
        }

        return declarationTemplateFiles.get(fileListBox.getSelectedIndex());
    }
}
