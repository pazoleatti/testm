package com.aplana.sbrf.taxaccounting.service;

import javax.sql.DataSource;

public class DSHolderProxy {
    public static DataSource getDataSource(DSHolderService holder) {
        return holder.getMigrationDataSource();
    }
}
