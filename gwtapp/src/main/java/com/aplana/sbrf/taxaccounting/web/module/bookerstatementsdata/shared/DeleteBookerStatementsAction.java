package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class DeleteBookerStatementsAction extends UnsecuredActionImpl<DeleteBookerStatementsResult> implements ActionName {

    /** Тип бухотчетности (0, 1) */
    private int statementsKind;

    /** Идентификаторы записей для удаления */
    private List<Long> uniqueRecordIds;

    public List<Long> getUniqueRecordIds() {
        return uniqueRecordIds;
    }

    public void setUniqueRecordIds(List<Long> uniqueRecordIds) {
        this.uniqueRecordIds = uniqueRecordIds;
    }

    public int getStatementsKind() {
        return statementsKind;
    }

    public void setStatementsKind(int statementsKind) {
        this.statementsKind = statementsKind;
    }

    @Override
    public String getName() {
        return "Удаление бухгалтерской отчетности";
    }
}
