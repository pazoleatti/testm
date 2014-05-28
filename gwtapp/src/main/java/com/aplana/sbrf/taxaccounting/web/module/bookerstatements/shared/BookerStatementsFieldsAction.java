package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Запрос для заполнения полей формы создания бух отчетности.
 * @author lhaziev
 */
public class BookerStatementsFieldsAction extends UnsecuredActionImpl<BookerStatementsFieldsResult>{

    /**
     * Номера полей необходимых для заполнения
     */
    public enum FieldsNum{
        FIRST,
        SECOND
    }
    private int fieldId;
    private FieldsNum fieldsNum;
	private Long DepartmentId;
	private List<Integer> reportPeriodId;

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
}
