package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import com.aplana.sbrf.taxaccounting.model.util.DateUtils;

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
    protected String fieldNameInGenitiveCase() {
        return "Даты выплаты дохода";
    }

    @Override
    protected String warningText() {
        return String.format("Раздел 2. Строка %s. Дата выплаты дохода: \" __ \" не может быть заменена значением \"%s\", т.к. строка не является строкой выплаты дохода.",
                income.getRowNum(),
                DateUtils.formatPossibleZeroDate(getDateToSet())
        );
    }
}
