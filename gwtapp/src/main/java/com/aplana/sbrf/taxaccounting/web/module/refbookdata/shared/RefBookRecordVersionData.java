package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * Информация о версиях элемента справочника
 * @author dloshkarev
 */
public class RefBookRecordVersionData implements Serializable {
    private static final long serialVersionUID = -8206369715220060886L;

    /** Дата начала действия версии */
    private Date versionStart;

    /** Дата конца действия версии */
    private Date versionEnd;

    /** Количество существующих версий элемента справочника */
    private int versionCount;

    public Date getVersionStart() {
        return versionStart;
    }

    public void setVersionStart(Date versionStart) {
        this.versionStart = versionStart;
    }

    public Date getVersionEnd() {
        return versionEnd;
    }

    public void setVersionEnd(Date versionEnd) {
        this.versionEnd = versionEnd;
    }

    public int getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }

    @Override
    public String toString() {
        return "RefBookRecordVersionData{" +
                "versionStart=" + versionStart +
                ", versionEnd=" + versionEnd +
                ", versionCount=" + versionCount +
                '}';
    }
}
