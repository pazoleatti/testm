package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * View для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 */
public class DepartmentConfigView extends ViewWithUiHandlers<DepartmentConfigUiHandlers>
        implements DepartmentConfigPresenter.MyView, Editor<Department> {

    interface Binder extends UiBinder<Widget, DepartmentConfigView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<Department, DepartmentConfigView> {
    }

    private final MyDriver driver = GWT.create(MyDriver.class);

//    @UiField
//    @Ignore
//    Label title;

    @Inject
    @UiConstructor
    public DepartmentConfigView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        driver.initialize(this);
    }
}