package com.aplana.sbrf.taxaccounting.scheduler.api.form;

import java.util.Date;

/**
 * Элемент формы для ввода даты
 */
public class DateBox extends FormElement{
    private Date defaultDate;

    public Date getDefaultDate() {
        return defaultDate;
    }

    public void setDefaultDate(Date defaultDate) {
        this.defaultDate = defaultDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DateBox dateBox = (DateBox) o;

        if (defaultDate != null ? !defaultDate.equals(dateBox.defaultDate) : dateBox.defaultDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (defaultDate != null ? defaultDate.hashCode() : 0);
        return result;
    }
}
