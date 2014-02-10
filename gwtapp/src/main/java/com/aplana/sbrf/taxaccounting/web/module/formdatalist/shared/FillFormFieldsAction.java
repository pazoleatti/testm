package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Запрос для заполнения полей формы создания НФ.
 * User: avanteev
 */
public class FillFormFieldsAction extends UnsecuredActionImpl<FillFormFieldsResult>{

    /**
     * Номера полей необходимых для заполнения
     */
    public enum FieldsNum{
        FIRST,
        SECOND,
        THIRD,
        FORTH
    }
    private int fieldId;
    private TaxType taxType;
    private FieldsNum fieldsNum;

    public FieldsNum getFieldsNum() {
        return fieldsNum;
    }

    public void setFieldsNum(FieldsNum fieldsNum) {
        this.fieldsNum = fieldsNum;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}
