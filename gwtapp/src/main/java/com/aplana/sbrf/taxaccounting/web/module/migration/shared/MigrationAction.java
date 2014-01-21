package com.aplana.sbrf.taxaccounting.web.module.migration.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Dmitriy Levykin
 */
public class MigrationAction extends UnsecuredActionImpl<MigrationResult> implements ActionName {

    private long[] rnus;
    private long[] years;

    public MigrationAction() {
    }

    public MigrationAction(long[] rnus, long[] years) {
        this.rnus = rnus;
        this.years = years;
    }

    public long[] getRnus() {
        return rnus;
    }

    public void setRnus(long[] rnus) {
        this.rnus = rnus;
    }

    public long[] getYears() {
        return years;
    }

    public void setYears(long[] years) {
        this.years = years;
    }

    @Override
    public String getName() {
        return "Миграция исторических данных";
    }
}
