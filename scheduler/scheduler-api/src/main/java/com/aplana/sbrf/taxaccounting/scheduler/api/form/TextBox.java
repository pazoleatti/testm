package com.aplana.sbrf.taxaccounting.scheduler.api.form;

/**
 * Элемент формы - текстовое поле
 */
public class TextBox extends FormElement{
    private String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TextBox textBox = (TextBox) o;

        if (defaultValue != null ? !defaultValue.equals(textBox.defaultValue) : textBox.defaultValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }
}
