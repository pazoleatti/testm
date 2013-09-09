package com.aplana.sbrf.taxaccounting.web.module.migration.shared;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.MigrationSendResult;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Dmitriy Levykin
 */
public class MigrationResult implements Result {

    private MigrationSendResult result;

    public int getSendFilesCount() {
        if (result == null) return 0;
        return result.getSendFilesCount();
    }

    public List<Exemplar> getExemplarList() {
        if (result == null) return null;
        return result.getExemplarList();
    }

    public void setResult(MigrationSendResult result) {
        this.result = result;
    }
}
