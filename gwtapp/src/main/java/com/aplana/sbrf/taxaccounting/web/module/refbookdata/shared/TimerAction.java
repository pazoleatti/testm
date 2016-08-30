package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Обновление статуса асинх. задач
 * User: lhaziev
 */
public class TimerAction extends UnsecuredActionImpl<TimerResult> implements ActionName {
    private long refBookId;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    @Override
    public String getName() {
        return "Обновление статуса задач";
    }
}
