package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.google.gwt.view.client.Range;

import java.util.List;

/**
 * Интерфейс общий для презентер-виджетов в справочнике.
 */
public interface ILinearRefBookData {
    public void updateTable();
    // позиция выделенной строки в таблице
    RefBookDataRow getSelectedRow();
    Integer getSelectedRowIndex();
    void setTableColumns(final List<RefBookColumn> columns);
    void setRange(Range range);
    int getPageSize();
    void blockDataView(FormMode mode);
    void setMode(FormMode mode);
    void setRefBookId(Long refBookId);
}
