package com.aplana.sbrf.taxaccounting.scheduler.api.form;

/**
 * Элемент формы CheckBox
 */
public class CheckBox extends FormElement {
    private boolean isChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CheckBox checkBox = (CheckBox) o;

        if (isChecked != checkBox.isChecked) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isChecked ? 1 : 0);
        return result;
    }
}
