package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.NumberFormat;

/** @author Vitalii Samolovskikh */
public class EditNumericColumn extends EditTextColumn {
    private static final NumberFormat FORMAT = NumberFormat.getFormat("#.##");

    public EditNumericColumn() {
        super();
        init();
    }

    public EditNumericColumn(String alias) {
        super(alias);
        init();
    }

    private void init() {
        this.setFieldUpdater(new FieldUpdater<DataRow, String>() {
            @Override
            public void update(int i, DataRow dataRow, String s) {
                dataRow.put(getAlias(), FORMAT.parse(s));
            }
        });
    }
}
