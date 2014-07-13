package com.aplana.sbrf.taxaccounting.web.module.scheduler.client.taskparams;

import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * Виджет обертка над CheckBox'ом,
 * является частным случаем ParamWidget
 *
 * @author auldanov
 */
public class WCheckBox extends ParamWidget{

    private CheckBox checkBox;

    public WCheckBox() {
        checkBox = new CheckBox();
    }

    @Override
    Widget getWidget() {
        return checkBox;
    }

    @Override
    boolean isValid() {
        return true;
    }

    @Override
    public String getErrorMsg() {
        return null;
    }

    @Override
    public String getValue() {
        return checkBox.getValue().toString();
    }

    @Override
    public void setValue(String value) {
        checkBox.setValue(Boolean.valueOf(value));
    }

    @Override
    public void setEnable(boolean enable) {
        checkBox.setEnabled(enable);
    }
}
