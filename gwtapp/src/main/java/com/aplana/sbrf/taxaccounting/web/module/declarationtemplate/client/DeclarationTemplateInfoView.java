package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.gwt.client.mask.ui.YearMaskBox;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.JrxmlFileExistEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DeclarationTemplateInfoView extends ViewWithUiHandlers<DeclarationTemplateInfoUiHandlers>
		implements DeclarationTemplateInfoPresenter.MyView {

    public interface Binder extends UiBinder<Widget, DeclarationTemplateInfoView> { }

    interface UrlTemplates extends SafeHtmlTemplates {
        @Template("download/downloadByUuid/{0}")
        SafeHtml getDownloadUrl(String uuid);

        @Template("download/declarationTemplate/downloadDect/{0}")
        SafeHtml getDownloadDTUrl(int dtId);

        @Template("download/uploadJrxml/{0}")
        SafeHtml getUploadJrxmlUrl(int dtId);

        @Template("download/uploadXsd/{0}")
        SafeHtml getUploadXsdlUrl(int dtId);

        @Template("download/declarationTemplate/uploadDect/{0}")
        SafeHtml getUploadDTUrl(int dtId);
    }

    private static final UrlTemplates urlTemplates = GWT.create(UrlTemplates.class);
    //private final MyDriver driver = GWT.create(MyDriver.class);

    private final static int DEFAULT_TABLE_TOP_POSITION = 140;
    private final static int LOCK_INFO_BLOCK_HEIGHT = 25;

    @UiField
    //@Path("declarationTemplate.version")
    YearMaskBox versionDateBegin;

    @UiField
    //@Path("endDate")
    YearMaskBox versionDateEnd;

    @UiField
    @Editor.Ignore
    FileUploadWidget uploadJrxmlFile, uploadXsdFile;

    @UiField
    //@Path("declarationTemplate.name")
            TextBox decName;

    @UiField
    @Editor.Ignore
    Anchor downloadJrxmlButton, downloadXsd;

    @UiField
    LinkButton deleteXsd, deleteJrxml;

    @UiField
    RefBookPickerWidget formKindPicker, formTypePicker;

    @Inject
    @UiConstructor
    public DeclarationTemplateInfoView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        FormElement.as(uploadJrxmlFile.getElement()).setAcceptCharset("UTF-8");
        FormElement.as(uploadXsdFile.getElement()).setAcceptCharset("UTF-8");
        //driver.initialize(this);
        formKindPicker.setPeriodDates(new Date(), new Date());
        formTypePicker.setPeriodDates(new Date(), new Date());
    }

    @Override
    public void setDeclarationTemplate(final DeclarationTemplateExt declarationTemplateExt) {
        /*uploadDectForm.reset();*/
        /*uploadJrxmlForm.reset();*/
        Integer id = declarationTemplateExt.getDeclarationTemplate().getId() != null ?
                declarationTemplateExt.getDeclarationTemplate().getId() : 0;
        DeclarationTemplate template = declarationTemplateExt.getDeclarationTemplate();
        uploadJrxmlFile.setActionUrl(urlTemplates.getUploadJrxmlUrl(id).asString());
        uploadXsdFile.setActionUrl(urlTemplates.getUploadXsdlUrl(id).asString());
        //driver.edit(declarationTemplateExt);
        decName.setText(declarationTemplateExt.getDeclarationTemplate().getName());
        versionDateBegin.setValue(declarationTemplateExt.getDeclarationTemplate().getVersion());
        versionDateEnd.setValue(declarationTemplateExt.getEndDate());

        setEnabled(template.getId() != null);
        downloadJrxmlButton.setEnabled(template.getJrxmlBlobId() != null);
        deleteJrxml.setEnabled(template.getJrxmlBlobId() != null);
        downloadXsd.setEnabled(template.getXsdId() != null);
        deleteXsd.setEnabled(template.getXsdId() != null);
        formKindPicker.setSingleValue(declarationTemplateExt.getDeclarationTemplate().getDeclarationFormKind().getId());
        formTypePicker.setFilter("TAX_KIND = '"+declarationTemplateExt.getDeclarationTemplate().getType().getTaxType().getCode()+"'");
        formTypePicker.setSingleValue(declarationTemplateExt.getDeclarationTemplate().getDeclarationFormTypeId());
    }

    @UiHandler("formKindPicker")
    public void onFormKindChange(ValueChangeEvent<List<Long>> event) {
        if (event.getValue()!= null && !event.getValue().isEmpty()) {
            getUiHandlers().setFormKind(event.getValue().get(0));
        } else {
            getUiHandlers().setFormKind(null);
        }
    }

    @UiHandler("formTypePicker")
    public void onFormTypeChange(ValueChangeEvent<List<Long>> event) {
        if (event.getValue()!= null && !event.getValue().isEmpty()) {
            getUiHandlers().setFormType(event.getValue().get(0));
        } else {
            getUiHandlers().setFormType(null);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandlerJrxml(ValueChangeHandler<String> valueChangeHandler) {
        return uploadJrxmlFile.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public HandlerRegistration addValueChangeHandlerXsd(ValueChangeHandler<String> valueChangeHandler) {
        return uploadXsdFile.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public HandlerRegistration addEndLoadHandlerXsd(EndLoadFileEvent.EndLoadFileHandler handler) {
        return uploadXsdFile.addEndLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addEndLoadHandlerJrxml(EndLoadFileEvent.EndLoadFileHandler handler) {
        return uploadJrxmlFile.addEndLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addJrxmlLoadHandler(JrxmlFileExistEvent.JrxmlFileExistHandler handler) {
        return uploadJrxmlFile.addJrxmlLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addStartLoadHandlerXsd(StartLoadFileEvent.StartLoadFileHandler handler) {
        return uploadXsdFile.addStartLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addStartLoadHandlerJrxml(StartLoadFileEvent.StartLoadFileHandler handler) {
        return uploadJrxmlFile.addStartLoadHandler(handler);
    }

    private void setEnabled(boolean isEnable){
        uploadJrxmlFile.setEnabled(isEnable);
        uploadXsdFile.setEnabled(isEnable);
    }

    @UiHandler("decName")
    void onDecNameChanged(ChangeEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onInfoChanged();
        }
    }

    @UiHandler("versionDateBegin")
    void onDateBeginChanged(ValueChangeEvent<Date> event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onInfoChanged();
        }
    }

    @UiHandler("versionDateEnd")
    void onDateEndChanged(ValueChangeEvent<Date> event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onInfoChanged();
        }
    }

    @UiHandler("downloadJrxmlButton")
    void onDownloadJrxmlButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().downloadJrxml();
        }
    }

    @UiHandler("downloadXsd")
    void onDownloadXsdClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().downloadXsd();
        }
    }

    @UiHandler("deleteXsd")
    void onDeleteXsd(ClickEvent event){
        Dialog.confirmMessage("Удаление xsd файла", "Вы действительно хотите удалить xsd-файл?", new DialogHandler() {
            @Override
            public void yes() {
                getUiHandlers().onDeleteXsd();
            }
        });
    }

    @UiHandler("deleteJrxml")
    void onDeleteJrxml(ClickEvent event){
        Dialog.confirmMessage("Удаление jrxml файла", "Вы действительно хотите удалить jrxml файл?", new DialogHandler() {
            @Override
            public void yes() {
                getUiHandlers().onCheckBeforeDeleteJrxml();
            }
        });
    }

    @Override
    public String getName() {
        return decName.getValue();
    }

    @Override
    public Date getEndDate() {
        return versionDateEnd.getValue();
    }

    @Override
    public Date getStartDate() {
        return versionDateBegin.getValue();
    }
}
