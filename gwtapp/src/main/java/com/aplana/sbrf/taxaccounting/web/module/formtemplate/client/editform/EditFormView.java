package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTypeTemplate;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class EditFormView extends ViewWithUiHandlers<EditFormUiHandlers>
        implements EditFormPresenter.MyView, Editor<FormTypeTemplate> {


    public interface Binder extends UiBinder<Widget, EditFormView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormTypeTemplate, EditFormView> {}
    private MyDriver driver;

    @UiField
    TextBox formTypeName;

    @UiField
    TextBox formTypeCode;

    @UiField
    TextBox ifrsName;

    @UiField
    CheckBox isIfrs;

    @Ignore
    @UiField
    HorizontalPanel ifrsPanel;

    @Ignore
    @UiField
    HorizontalPanel ifrsNamePanel;

    @Inject
    public EditFormView(Binder uiBinder, MyDriver driver) {
        initWidget(uiBinder.createAndBindUi(this));

        this.driver = driver;
        this.driver.initialize(this);

        isIfrs.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ifrsNamePanel.setVisible(event.getValue());
                if (!event.getValue()) ifrsName.setText("");
            }
        });
    }

    @UiHandler("cancel")
    public void onCancel(ClickEvent event) {
        if (driver.isDirty()){
            Dialog.confirmMessage("Редактирование макета", "Сохранить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    getUiHandlers().onSave();
                }

                @Override
                public void no() {
                    getUiHandlers().onCancel();
                }
            });
        } else {
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
    public FormTypeTemplate getTypeData() {
        return driver.flush();
    }

    @Override
    public void edit(FormTypeTemplate type) {
        driver.edit(type);
    }

    @Override
    public void changeItem(final FormTypeTemplate type) {
        if (driver.isDirty()){
            Dialog.confirmMessage("Редактирование макета", "Сохранить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    getUiHandlers().onSave();
                    doChange(type);
                }

                @Override
                public void no() {
                    getUiHandlers().setModel(type);
                    getUiHandlers().onCancel();
                }
            });
        } else {
            doChange(type);
        }
    }

    private void doChange(FormTypeTemplate type){
        driver.edit(type);
        if (TaxType.INCOME.equals(type.getTaxType())) {
            ifrsPanel.setVisible(true);
            if (type.getIsIfrs() != null) ifrsNamePanel.setVisible(type.getIsIfrs());
        } else {
            ifrsPanel.setVisible(false);
            ifrsNamePanel.setVisible(false);
        }
        getUiHandlers().setModel(type);
    }

    @UiHandler("formTypeCode")
    public void onFormTypeCodeKeyUp(ChangeEvent event){
        if (formTypeCode.getValue().length() > 9){
            Dialog.infoMessage("Длина кода формы должна быть от 0 до 9 символов.");
            formTypeCode.setValue(formTypeCode.getValue().substring(0, 9));
        }
    }

    @Override
    public boolean checkIfrs() {
        return !(getTypeData().getIsIfrs() && (getTypeData().getIfrsName() == null || getTypeData().getIfrsName().isEmpty()));
    }
}
