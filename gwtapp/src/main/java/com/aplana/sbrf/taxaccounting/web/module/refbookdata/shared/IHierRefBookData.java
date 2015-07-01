package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import java.util.Date;

/**
 * User: avanteev
 */
public interface IHierRefBookData {
    /**
     * Для иерархических
     */
    void setRecordItem(RecordChanges recordChanges);
    void initPickerState(Date relevanceDate, String searchPattern);
    void setMode(FormMode mode);
    void updateTree();
    void setRefBookId(Long refBookId);
    void clearAll();
    void clearFilter();
    void loadAndSelect();
    void setAttributeId(Long attrId);
}
