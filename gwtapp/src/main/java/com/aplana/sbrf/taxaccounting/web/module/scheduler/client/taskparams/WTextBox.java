package com.aplana.sbrf.taxaccounting.web.module.scheduler.client.taskparams;

import com.aplana.gwt.client.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Виджет обертка над TextBox'ом,
 * является частным случаем ParamWidget
 *
 * @author auldanov
 */
public class WTextBox extends ParamWidget {
    private TextBox textBox;
    private String errorMessage;

    public WTextBox() {
        textBox = new TextBox();
    }

    public void setWidth(String width){
        textBox.setWidth(width);
    }

    @Override
    Widget getWidget() {
        return textBox;
    }

    @Override
    boolean isValid() {
        String value = textBox.getValue();
        errorMessage = "";
        if (isRequired() && (value == null || value.isEmpty())){
            errorMessage += "Поле «" + getName() + "» обязательна для заполнения";
            return false;
        } else {
            if (value != null && !value.isEmpty()){
                switch (getType()){
                    case INT:
                        try {
                            Integer.parseInt(value);
                        } catch (NumberFormatException e){
                            errorMessage += "Параметр «" + getName() + "» должен иметь целочисленное значение";
                        }
                        break;
                    case LONG:
                        try {
                            Long.parseLong(value);
                        } catch (NumberFormatException e){
                            errorMessage += "Параметр «" + getName() + "» должен содержать целочисленное значение";
                        }
                        break;
                    case FLOAT:
                        try {
                            Float.parseFloat(value);
                        } catch (NumberFormatException e){
                            errorMessage += "Параметр «" + getName() + "» должен содержать вещественное значение";
                        }
                        break;
                    case DOUBLE:
                        try {
                            Double.parseDouble(value);
                        } catch (NumberFormatException e){
                            errorMessage += "Параметр «" + getName() + "» должен содержать вещественное значение";
                        }
                        break;
                    case STRING:
                        break;
                    default:
                        errorMessage += "Ошибка при определении типа значения, для поля «" + getName() + "»";
                }

                return errorMessage.isEmpty();
            } else{
                return true;
            }
        }
    }

    @Override
    public String getErrorMsg() {
        return errorMessage;
    }

    @Override
    public String getValue() {
        return textBox.getValue();
    }

    @Override
    public void setValue(String value) {
        textBox.setValue(value);
    }

    @Override
    public void setEnable(boolean enable) {
        textBox.setEnabled(enable);
    }
}
