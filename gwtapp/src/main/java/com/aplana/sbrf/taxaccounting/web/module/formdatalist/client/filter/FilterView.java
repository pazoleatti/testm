package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.DataFilter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter.SelectItem;
import com.aplana.sbrf.taxaccounting.web.widget.listeditor.client.LongListBoxEditor;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import java.util.List;

public class FilterView extends ViewImpl implements FilterPresenter.MyView, Editor<DataFilter>{

    interface MyBinder extends UiBinder<Widget, FilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<DataFilter, FilterView>{
    }

    private final Widget widget;

    private final MyDriver driver;

    @UiField
	LongListBoxEditor period;

    @UiField
	LongListBoxEditor department;

    @UiField
	LongListBoxEditor formtype;

    @UiField
	LongListBoxEditor kind;

    @Inject
    public FilterView(final MyBinder binder, final MyDriver driver) {
        widget = binder.createAndBindUi(this);
        this.driver = driver;
        this.driver.initialize(this);
    }


    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public void setDataFilter(DataFilter dataFilter) {
        driver.edit(dataFilter);
    }


    @Override
    public DataFilter getDataFilter() {
        return driver.flush();
    }


    @Override
    public void setPeriodList(List<SelectItem> list){
        for (SelectItem selectItem : list) {
            period.addItem(selectItem.getName(), String.valueOf(selectItem.getId()));
        }
    }

    @Override
    public void setDepartmentList(List<SelectItem> list) {
        for (SelectItem selectItem : list) {
            department.addItem(selectItem.getName(), String.valueOf(selectItem.getId()));
        }
    }


    @Override
    public void setFormtypeList(List<SelectItem> list) {
        for (SelectItem selectItem : list) {
            formtype.addItem(selectItem.getName(), String.valueOf(selectItem.getId()));
        }
    }

    @Override
    public void setKindList(List<SelectItem> list) {
        for (SelectItem selectItem : list) {
            kind.addItem(selectItem.getName(), String.valueOf(selectItem.getId()));
        }
    }

    @Override
    public void clearData(){
        period.clear();
        department.clear();
        formtype.clear();
        kind.clear();
    }

}
