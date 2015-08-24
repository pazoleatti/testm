package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import java.util.List;

/**
 * Интерфейс общий для презентер-виджетов в справочнике.
 */
public interface ILinearRefBookData {
    void updateTable();
    void setTableColumns(final List<RefBookColumn> columns);
    void setMode(FormMode mode);
    void setRefBookId(Long refBookId);
}
