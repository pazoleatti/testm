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
    public static final String FORMAT = "dd-MM-yyyy, HH:mm";

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

    public void setWidth(String width){
        datePicker.setWidth(width);
    }

    @Override
    public String getErrorMsg() {
        return null;
    }

    @Override
    public String getValue() {
        return datePicker.getValue() != null ? DateTimeFormat.getFormat(FORMAT).format(datePicker.getValue()):null;
    }

    @Override
    public void setValue(String date) {
        if (date != null){
            datePicker.setValue(DateTimeFormat.getFormat(FORMAT).parse(date));
        }
    }

    @Override
    public void setEnable(boolean enable) {
        datePicker.setEnabled(enable);
    }
}
