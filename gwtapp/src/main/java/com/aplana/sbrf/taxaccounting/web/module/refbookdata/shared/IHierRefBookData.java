package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

/**
 * User: avanteev
 */
public interface IHierRefBookData {
    /**
     * Для иерархических
     */
    void setMode(FormMode mode);
    void updateTree();
    void setRefBookId(Long refBookId);
    void setAttributeId(Long attrId);
}
