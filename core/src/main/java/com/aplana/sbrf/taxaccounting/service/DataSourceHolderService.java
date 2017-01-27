package com.aplana.sbrf.taxaccounting.service;

import javax.sql.DataSource;

/**
 * Сервис ejb-холдера для проброса датасурсов из ejb в spring
 *
 * @author Dmitriy Levykin
 */
public interface DataSourceHolderService {
    DataSource getApplicationDataSource();
}