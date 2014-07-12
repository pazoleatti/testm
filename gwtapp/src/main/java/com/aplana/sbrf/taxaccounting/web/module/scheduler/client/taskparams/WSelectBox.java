package com.aplana.sbrf.taxaccounting.web.module.scheduler.client.taskparams;

import com.aplana.gwt.client.ValueListBox;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.SelectBoxItem;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * Виджет обертка над ValueListBox<SelectBoxItem>'ом,
 * является частным случаем ParamWidget
 *
 * @author auldanov
 */
public class WSelectBox extends ParamWidget {

    private String errorMessage;

    private ValueListBox<SelectBoxItem> listBox;

    private List<SelectBoxItem> values;

    public WSelectBox(List<SelectBoxItem> values) {
        listBox = new ValueListBox<SelectBoxItem>(new AbstractRenderer<SelectBoxItem>() {
            @Override
            public String render(SelectBoxItem object) {
                if (object != null){
                    return object.getLabel();
                } else
                    return "";
            }
        });

        this.values = values;
        listBox.setAcceptableValues(values);
    }

    public void setWidth(String width){
        listBox.setWidth(width);
    }

    @Override
    public Widget getWidget() {
        return listBox;
    }

    @Override
    public boolean isValid() {
        if (isRequired() && listBox.getValue() == null){
            errorMessage = "Поле " + getName() + " обязательно для заполненеия";
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getErrorMsg() {
        return errorMessage;
    }

    @Override
    public String getValue() {
        return listBox.getValue() != null ? listBox.getValue().getId().toString() : null;
    }

    @Override
    public void setValue(String value) {
        Integer id = Integer.valueOf(value);
        for (SelectBoxItem selectBoxItem : values) {
            if (selectBoxItem.getId().equals(id)){
                listBox.setValue(selectBoxItem);
                return;
            }
        }
    }
}
