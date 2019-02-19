package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;


public class DateEditorFactory {

    public static DateEditor getEditor(EditableDateField dateField) {
        switch (dateField) {
            case ACCRUED:
                return new AccruedDateEditor();
            case PAYOUT:
                return new PayoutDateEditor();
            case TAX:
                return new TaxDateEditor();
            case TRANSFER:
                return new TransferDateEditor();

            default:
                throw new IllegalArgumentException("Не установлен редактор для переданного поля");
        }
    }
}
