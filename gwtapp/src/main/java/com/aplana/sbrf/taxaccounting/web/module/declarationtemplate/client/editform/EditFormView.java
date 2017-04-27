package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.editform;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
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
        implements EditFormPresenter.MyView, Editor<DeclarationType> {


    public interface Binder extends UiBinder<Widget, EditFormView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<DeclarationType, EditFormView> {}
    private MyDriver driver;

    @UiField
    TextBox name;

    @Inject
    public EditFormView(Binder uiBinder, MyDriver driver) {
        initWidget(uiBinder.createAndBindUi(this));

        this.driver = driver;
        this.driver.initialize(this);
        driver.edit(new DeclarationType());
    }

    @UiHandler("cancel")
    public void onCancel(ClickEvent event) {
        if (getUiHandlers() != null) {
            if (driver.isDirty()){
                getUiHandlers().onCancel();
            }
        }
    }

    @UiHandler("save")
    public void onSave(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onSave();
        }
    }

    @Override
    public DeclarationType getDecTypeData() {
        return driver.flush();
    }

    @Override
    public boolean isChangeFilter() {
        return driver.isDirty();
    }

    @Override
    public void edit(DeclarationType type) {
        driver.edit(type);
    }
}
