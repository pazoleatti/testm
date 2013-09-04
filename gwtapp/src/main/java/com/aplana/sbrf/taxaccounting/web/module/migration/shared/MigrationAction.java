package com.aplana.sbrf.taxaccounting.web.module.migration.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   @author Dmitriy Levykin
 */
public class MigrationAction extends UnsecuredActionImpl<MigrationResult> implements ActionName {

    private long[] rnus;

    public MigrationAction() {
    }

    public MigrationAction(long[] rnus) {
        this.rnus = rnus;
    }

    public long[] getRnus() {
        return rnus;
    }

    public void setRnus(long[] rnus) {
        this.rnus = rnus;
    }

    @Override
    public String getName() {
        return "Миграция исторических данных";
    }
}
