package com.aplana.sbrf.taxaccounting.mdb;

import com.aplana.sbrf.taxaccounting.service.DSHolderService;

import javax.sql.DataSource;

public class DSHolderProxy {
    public static DataSource getDataSource(DSHolderService holder) {
        return holder.getApplicationDataSource();
    }
}
