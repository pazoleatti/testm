package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.editform;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class EditFormView extends ViewWithUiHandlers<EditFormUiHandlers>
        implements EditFormPresenter.MyView {


    public interface Binder extends UiBinder<Widget, EditFormView> {
    }

    @UiField
    TextBox formType;

    @UiField
    TextBox formTypeCode;

    @Inject
    public EditFormView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("cancel")
    public void onCancel(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCancel();
        }
    }

    @UiHandler("save")
    public void onSave(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onSave();
        }
    }

    @Override
    public void setFormTypeName(String formTypeName) {
        formType.setValue(formTypeName);
    }

    @Override
    public void setFormTypeCode(String formTypeCode) {
        this.formTypeCode.setValue(formTypeCode);
    }

    @Override
    public String getFormTypeName() {
        return formType.getValue();
    }

    @Override
    public String getFormTypeCode() {
        return formTypeCode.getValue();
    }
}
