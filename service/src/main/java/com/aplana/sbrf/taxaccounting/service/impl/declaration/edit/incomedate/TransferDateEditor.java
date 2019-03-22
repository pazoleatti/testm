package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import com.aplana.sbrf.taxaccounting.model.util.DateUtils;

import java.util.Date;

class TransferDateEditor extends DateEditor {

    @Override
    Date getDateToEdit() {
        return this.income.getTaxTransferDate();
    }

    @Override
    Date getDateToSet() {
        return this.incomeDatesDTO.getTransferDate();
    }

    @Override
    void editDate() {
        this.income.setTaxTransferDate(getDateToSet());
    }

    @Override
    String fieldNameInGenitiveCase() {
        return "Срока перечисления";
    }

    @Override
    String warningText() {
        return String.format("Раздел 2. Строка %s. Срок перечисления: \" __ \" не может быть заменен значением \"%s\", т.к. строка не является строкой выплаты дохода либо перечисления в бюджет.",
                income.getRowNum(),
                DateUtils.formatPossibleZeroDate(getDateToSet())
        );
    }
}
