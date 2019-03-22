package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import com.aplana.sbrf.taxaccounting.model.util.DateUtils;

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
    protected String fieldNameInGenitiveCase() {
        return "Даты НДФЛ";
    }

    @Override
    protected String warningText() {
        return String.format("Раздел 2. Строка %s. Дата НДФЛ: \" __ \" не может быть заменена значением \"%s\", т.к. строка не является строкой начисления либо выплаты дохода.",
                income.getRowNum(),
                DateUtils.formatPossibleZeroDate(getDateToSet())
        );
    }
}
