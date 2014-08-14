package com.aplana.sbrf.taxaccounting.scheduler;
import com.aplana.sbrf.taxaccounting.service.DataSourceHolderService;

import javax.sql.DataSource;

/**
 * Класс для получения датасурса приложения из ejb-модуля
 *
 */
public class ApplicationDataSource {
    public static DataSource getDataSource(DataSourceHolderService holder) {
        return holder.getApplicationDataSource();
    }
}
