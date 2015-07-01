package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

/**
 * User: avanteev
 */
public class HierRefBookExecutor implements IRefBookExecutor {

    IHierRefBookData hierRefBookData;

    public HierRefBookExecutor(IHierRefBookData hierRefBookData) {
        this.hierRefBookData = hierRefBookData;
    }

    @Override
    public void updateData() {
        hierRefBookData.updateTree();
    }

    @Override
    public void setMode(FormMode mode) {
        hierRefBookData.setMode(mode);
    }
}
