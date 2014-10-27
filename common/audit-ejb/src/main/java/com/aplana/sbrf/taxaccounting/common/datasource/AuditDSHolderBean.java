package com.aplana.sbrf.taxaccounting.common.datasource;

import com.aplana.sbrf.taxaccounting.service.DataSourceHolderService;
import com.aplana.sbrf.taxaccounting.service.DataSourceHolderServiceLocal;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.sql.DataSource;

/**
 * ejb-бин для проброса датасурсов в spring
 *
 */
@Stateless
@Local(DataSourceHolderServiceLocal.class)
public class AuditDSHolderBean implements DataSourceHolderService {
    @Resource(name = "jdbc/TaxAccDS")
    DataSource applicationDataSource;

    @Override
    public DataSource getApplicationDataSource() {
        return applicationDataSource;
    }

    @Override
    public DataSource getMigrationDataSource() {
        return null;
    }
}
