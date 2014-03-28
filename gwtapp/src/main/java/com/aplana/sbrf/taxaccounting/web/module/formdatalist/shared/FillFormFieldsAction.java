package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

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
	private Long DepartmentId;
	private List<Integer> reportPeriodId;
    private List<FormDataKind> kinds;

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

	public Long getDepartmentId() {
		return DepartmentId;
	}

	public void setDepartmentId(Long departmentId) {
		DepartmentId = departmentId;
	}

	public List<Integer> getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(List<Integer> reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

    public List<FormDataKind> getKinds() {
        return kinds;
    }

    public void setKinds(List<FormDataKind> kinds) {
        this.kinds = kinds;
    }
}
