package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

/**
 * @author Andrey Drunk
 */
public class AttributeChangeEvent {

    /**
     * Конструктор для события
     *
     * @param attrName алиас
     * @param newValue значени
     */
    public AttributeChangeEvent(String attrName, Object newValue) {
        this.attrName = attrName;
        this.newValue = newValue;
    }

    /**
     * Тип мобытия при изменения значния атрибута
     */
    public AttributeChangeEventType type = AttributeChangeEventType.IGNORED;

    /**
     * Наименование атрибута, алиас
     */
    private String attrName;

    /**
     * Текущее значение атрибута
     */
    private RefBookValue currentValue;

    /**
     * Новое значение атрибута
     */
    private Object newValue;


    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public RefBookValue getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(RefBookValue refBookValue) {
        this.currentValue = refBookValue;
    }

    public AttributeChangeEventType getType() {
        return type;
    }

    public void setType(AttributeChangeEventType type) {
        this.type = type;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object value) {
        this.newValue = value;
    }
}
