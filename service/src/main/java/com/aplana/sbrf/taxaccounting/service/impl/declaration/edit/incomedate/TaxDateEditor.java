package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import java.util.Date;

public class TaxDateEditor extends DateEditor {

    @Override
    protected Date getDateToEdit() {
        return this.income.getTaxDate();
    }

    @Override
    protected Date getDateToSet() {
        return this.incomeDatesDTO.getTaxDate();
    }

    @Override
    protected void editDate() {
        this.income.setTaxDate(getDateToSet());
    }

    @Override
    protected String fieldTitleForWarning() {
        return "Дата выплаты дохода";
    }

    @Override
    protected String fieldNameInGenitiveCase() {
        return "Даты НДФЛ";
    }

    @Override
    protected String rowNameInInstrumentalCase() {
        return "строкой начисления либо выплаты дохода";
    }

    @Override
    public String fieldName() {
        return "Дата НДФЛ";
    }
}
