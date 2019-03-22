package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import com.aplana.sbrf.taxaccounting.model.util.DateUtils;

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
    protected String fieldNameInGenitiveCase() {
        return "Даты начисления дохода";
    }

    @Override
    protected String warningText() {
        return String.format("Раздел 2. Строка %s. Дата начисления дохода: \" __ \" не может быть заменена значением \"%s\", т.к. строка не является строкой начисления дохода.",
                income.getRowNum(),
                DateUtils.formatPossibleZeroDate(getDateToSet())
        );
    }
}
