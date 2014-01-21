package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class DeleteRefBookRowResult implements Result {
    private String uuid;
    /** Уникальный идентификатор версии записи, тип которой совпадает с удаленной, а дата начала минимальна */
    private Long nextVersion;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getNextVersion() {
        return nextVersion;
    }

    public void setNextVersion(Long nextVersion) {
        this.nextVersion = nextVersion;
    }
}
