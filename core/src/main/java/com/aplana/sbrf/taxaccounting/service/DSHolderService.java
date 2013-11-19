package com.aplana.sbrf.taxaccounting.service;

import javax.sql.DataSource;

public interface DSHolderService {
    public DataSource getApplicationDataSource();

    public DataSource getMigrationDataSource();
}
