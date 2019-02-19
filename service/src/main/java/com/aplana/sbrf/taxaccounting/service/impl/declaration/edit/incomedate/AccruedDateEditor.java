package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import java.util.Date;

public class AccruedDateEditor extends DateEditor {

    @Override
    protected Date getDateToEdit() {
        return this.income.getIncomeAccruedDate();
    }

    @Override
    protected Date getDateToSet() {
        return this.incomeDatesDTO.getAccruedDate();
    }

    @Override
    protected void editDate() {
        this.income.setIncomeAccruedDate(getDateToSet());
    }

    @Override
    protected String fieldTitleForWarning() {
        return "Дата начисления дохода";
    }

    @Override
    protected String fieldNameInGenitiveCase() {
        return "Даты начисления дохода";
    }

    @Override
    protected String rowNameInInstrumentalCase() {
        return "строкой начисления дохода";
    }

    @Override
    public String fieldName() {
        return "Дата начисления дохода";
    }
}
