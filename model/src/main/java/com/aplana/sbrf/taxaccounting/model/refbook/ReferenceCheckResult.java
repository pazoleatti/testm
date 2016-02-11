package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.Date;

/**
 * Результат проверки корректности справочных аттрибутов
 * http://conf.aplana.com/pages/viewpage.action?pageId=23245326
 * @author dloshkarev
 */
public class ReferenceCheckResult implements Serializable {
    private static final long serialVersionUID = 8006826237606128847L;

    private Long recordId;
    private Date versionFrom;
    private Date versionTo;
    private CheckResult result;

    public ReferenceCheckResult() {
    }

    public ReferenceCheckResult(Long recordId, CheckResult result) {
        this.recordId = recordId;
        this.result = result;
    }

    public ReferenceCheckResult(Long recordId, Date versionFrom, Date versionTo, CheckResult result) {
        this.recordId = recordId;
        this.versionFrom = versionFrom;
        this.versionTo = versionTo;
        this.result = result;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Date getVersionFrom() {
        return versionFrom;
    }

    public void setVersionFrom(Date versionFrom) {
        this.versionFrom = versionFrom;
    }

    public Date getVersionTo() {
        return versionTo;
    }

    public void setVersionTo(Date versionTo) {
        this.versionTo = versionTo;
    }

    public CheckResult getResult() {
        return result;
    }

    public void setResult(CheckResult result) {
        this.result = result;
    }
}
