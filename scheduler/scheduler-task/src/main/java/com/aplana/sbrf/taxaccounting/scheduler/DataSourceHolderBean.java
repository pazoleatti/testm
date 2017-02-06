package com.aplana.sbrf.taxaccounting.scheduler;

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
public class DataSourceHolderBean implements DataSourceHolderService {
    @Resource(name = "jdbc/TaxAccDS")
    DataSource applicationDataSource;

    @Override
    public DataSource getApplicationDataSource() {
        return applicationDataSource;
    }
}
