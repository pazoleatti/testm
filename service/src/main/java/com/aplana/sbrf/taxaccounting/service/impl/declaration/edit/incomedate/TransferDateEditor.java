package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import java.util.Date;

public class TransferDateEditor extends DateEditor {

    @Override
    protected Date getDateToEdit() {
        return this.income.getTaxTransferDate();
    }

    @Override
    protected Date getDateToSet() {
        return this.incomeDatesDTO.getTransferDate();
    }

    @Override
    protected void editDate() {
        this.income.setTaxTransferDate(getDateToSet());
    }

    @Override
    protected String fieldTitleForWarning() {
        return "Дата выплаты дохода";
    }

    @Override
    protected String fieldNameInGenitiveCase() {
        return "Срока перечисления";
    }

    @Override
    protected String rowNameInInstrumentalCase() {
        return "строкой перечисления в бюджет";
    }

    @Override
    public String fieldName() {
        return "Срок перечисления";
    }
}
