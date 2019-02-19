package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import java.util.Date;

public class PayoutDateEditor extends DateEditor {

    @Override
    protected Date getDateToEdit() {
        return this.income.getIncomePayoutDate();
    }

    @Override
    protected Date getDateToSet() {
        return this.incomeDatesDTO.getPayoutDate();
    }

    @Override
    protected void editDate() {
        this.income.setIncomePayoutDate(getDateToSet());
    }

    @Override
    protected String fieldTitleForWarning() {
        return "Дата выплаты дохода";
    }

    @Override
    protected String fieldNameInGenitiveCase() {
        return "Даты выплаты дохода";
    }

    @Override
    protected String rowNameInInstrumentalCase() {
        return "строкой выплаты дохода";
    }

    @Override
    public String fieldName() {
        return "Дата выплаты дохода";
    }
}
