package com.aplana.sbrf.taxaccounting.web.module.scheduler.client.taskparams;

import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;

/**
 * Виджет обертка над DateMaskBoxPicker'ом,
 * является частным случаем ParamWidget
 *
 * @author auldanov
 */
public class WDateBox extends ParamWidget {
    private DateMaskBoxPicker datePicker;

    public WDateBox() {
        datePicker = new DateMaskBoxPicker();
        datePicker.setCanBeEmpty(!isRequired());
    }

    @Override
    Widget getWidget() {
        return datePicker;
    }

    @Override
    boolean isValid() {
        if (isRequired() && datePicker.getValue() == null){
            return false;
        } else{
            return true;
        }
    }

    @Override
    public String getErrorMsg() {
        return null;
    }

    @Override
    public String getValue() {
        return datePicker.getValue() != null ? datePicker.getValue().toString():null;
    }

    @Override
    public void setValue(String value) {
        datePicker.setValue(DateTimeFormat.getFormat("yyyy-MM-dd").parse(value));
    }
}
