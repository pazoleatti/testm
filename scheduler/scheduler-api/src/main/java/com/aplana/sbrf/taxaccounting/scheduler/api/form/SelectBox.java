package com.aplana.sbrf.taxaccounting.scheduler.api.form;

import java.io.Serializable;
import java.util.List;

/**
 * Элемент - выпадающий список
 */
public class SelectBox extends FormElement implements Serializable{
    private List<SelectBoxItem> values;

    public List<SelectBoxItem> getValues() {
        return values;
    }

    public void setValues(List<SelectBoxItem> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SelectBox selectBox = (SelectBox) o;

        if (values != null ? !values.equals(selectBox.values) : selectBox.values != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }
}
