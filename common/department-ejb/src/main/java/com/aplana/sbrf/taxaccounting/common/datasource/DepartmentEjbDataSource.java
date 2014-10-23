package com.aplana.sbrf.taxaccounting.common.datasource;
import com.aplana.sbrf.taxaccounting.service.DataSourceHolderService;

import javax.sql.DataSource;

/**
 * Класс для получения датасурса приложения из ejb-модуля  установки использования подразделения в модуле гарантий
 *
 */
public final class DepartmentEjbDataSource {

    private DepartmentEjbDataSource() {}

    public static DataSource getDataSource(DataSourceHolderService holder) {
        return holder.getApplicationDataSource();
    }
}
