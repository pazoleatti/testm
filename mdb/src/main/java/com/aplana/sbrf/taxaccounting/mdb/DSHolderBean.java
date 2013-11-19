package com.aplana.sbrf.taxaccounting.mdb;

import com.aplana.sbrf.taxaccounting.service.DSHolderService;
import com.aplana.sbrf.taxaccounting.service.DSHolderServiceLocal;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.sql.DataSource;

@Stateless
@Local(DSHolderServiceLocal.class)
public class DSHolderBean implements DSHolderService {
    @Resource(name = "jdbc/TaxAccDS")
    DataSource applicationDataSource;

    @Resource(name = "jdbc/TaxAccDS_MIGRATION")
    DataSource migrationDataSource;

    @Override
    public DataSource getApplicationDataSource() {
        return applicationDataSource;
    }

    @Override
    public DataSource getMigrationDataSource() {
        return migrationDataSource;
    }
}
