package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

/**
 * User: avanteev
 */
public class LinearRefBookExecutor implements IRefBookExecutor {
    ILinearRefBookData linearRefBookData;

    public LinearRefBookExecutor(ILinearRefBookData linearRefBookData) {
        this.linearRefBookData = linearRefBookData;
    }

    @Override
    public void updateData() {
        linearRefBookData.updateTable();
    }

    @Override
    public void setMode(FormMode mode) {
        linearRefBookData.setMode(mode);
    }
}
