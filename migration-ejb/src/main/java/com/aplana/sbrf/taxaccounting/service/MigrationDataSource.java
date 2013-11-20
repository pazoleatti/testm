package com.aplana.sbrf.taxaccounting.service;

import javax.sql.DataSource;

/**
 * Класс для получения датасурса миграции из ejb-модуля
 *
 * @author Dmitriy Levykin
 */
public class MigrationDataSource {
    public static DataSource getDataSource(DataSourceHolderService holder) {
        return holder.getMigrationDataSource();
    }
}
