package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.RefBookCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.google.gwt.cell.client.FieldUpdater;

import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class RefBookUiColumn extends DataRowColumn<Long> {

    public RefBookUiColumn(final RefBookColumn column, ColumnContext columnContext) {
        super(new RefBookCell(columnContext), column);
        this.setHorizontalAlignment(ALIGN_RIGHT);
        this.setFieldUpdater(new FieldUpdater<DataRow<Cell>, Long>() {
            @Override
            public void update(int index, DataRow<Cell> dataRow, Long value) {
                dataRow.put(getAlias(), value);

                // TODO Levykin Реализовать обновление RefBookDereference зависимых ячеек после http://jira.aplana.com/browse/SBRFACCTAX-5382. Пока зависимые ячейки только сбрасываются.
                // TODO Levykin http://jira.aplana.com/browse/SBRFACCTAX-5381
                List<Cell> linkedCells = dataRow.getLinkedCells(column.getId());
                if (linkedCells != null) {
                    for (Cell cell : linkedCells) {
                        cell.setRefBookDereference(null);
                    }
                }
                CellModifiedEvent event = new CellModifiedEvent(dataRow, linkedCells != null && !linkedCells.isEmpty());
                fireEvent(event);
            }
        });
    }

    @Override
    public Long getValue(DataRow<Cell> dataRow) {
        return (Long) dataRow.get(alias);
    }
}
