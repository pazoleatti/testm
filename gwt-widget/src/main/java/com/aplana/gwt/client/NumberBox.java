package com.aplana.gwt.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HasValue;

import java.math.BigDecimal;

/**
 * NumberBox, который в задимсабленном состоянии показывает Label.
 * @author lhaziev
 */
public class NumberBox extends TextBox {

    private NumberFormat numberFormat;

    public NumberBox(int precision) {
        super();
        StringBuilder mask = new StringBuilder("#,##0");
        if (precision > 0) {
            mask.append('.');
            for(int i = 0; i < precision; ++i) {
                mask.append('0');
            }
        }
        numberFormat = NumberFormat.getFormat(mask.toString());
    }

    @Override
    public void setValue(String value) {
        if (value != null && !value.isEmpty()) {
            setLabelValue(numberFormat.format(new BigDecimal(value)).replace(',', ' '));
        } else {
            setLabelValue(value);
        }
        ((HasValue)widget).setValue(value);
    }

    @Override
    protected void updateLabelValue() {
        String val = (String) ((HasValue) this).getValue();
        if (val != null && !val.isEmpty()) {
            try {
                setLabelValue(numberFormat.format(new BigDecimal(val)).replace(',', ' '));
            } catch (Exception e) {
                setLabelValue(val);
            }
        } else {
            setLabelValue(val);
        }
    }
}
