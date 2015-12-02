package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * Результат проверки пересечений для версий записей справочника
 * Собственно результатом являются 2 даты, которые соответствуют датам начала актуальности двух последовательных версий - текущей и следующей за ней.
 * При этом возможны случаи, когда текущая версия может быть фиктивной, т.е фактически являться датой окончания периода актуальности предыдущей версии
 * Статус версий необходим для последующей обработки результата, который собственно представлен в виде кода, по которому определяются действия,
 * которые будут выполнены с обнаруженными пересечениями
 * @author dloshkarev
 */
public class CheckCrossVersionsResult implements Serializable {
    private static final long serialVersionUID = 2680059745496101444L;

    /** Порядковый номер результата, необходим для обработки связок дат */
    private int num;
    /** Уникальный идентификатор версии записи справочника */
    private Long recordId;
    /** Дата начала актуальности версии */
    private Date version;
    /** Статус версии */
    private VersionedObjectStatus status;
    /** Дата начала актуальности следующей версии */
    private Date nextVersion;
    /** Статус следующей версии */
    private VersionedObjectStatus nextStatus;
    /** Результат проверки */
    private CrossResult result;

    public CheckCrossVersionsResult() {
    }

    public CheckCrossVersionsResult(int num, Long recordId, Date version, VersionedObjectStatus status, Date nextVersion, VersionedObjectStatus nextStatus, CrossResult result) {
        this.num = num;
        this.recordId = recordId;
        this.version = version;
        this.status = status;
        this.nextVersion = nextVersion;
        this.nextStatus = nextStatus;
        this.result = result;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    public Date getNextVersion() {
        return nextVersion;
    }

    public void setNextVersion(Date nextVersion) {
        this.nextVersion = nextVersion;
    }

    public VersionedObjectStatus getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(VersionedObjectStatus nextStatus) {
        this.nextStatus = nextStatus;
    }

    public CrossResult getResult() {
        return result;
    }

    public void setResult(CrossResult result) {
        this.result = result;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "CheckCrossVersionsResult{" +
                "num=" + num +
                ", recordId=" + recordId +
                ", version=" + version +
                ", status=" + status +
                ", nextVersion=" + nextVersion +
                ", nextStatus=" + nextStatus +
                ", result=" + result +
                '}';
    }
}
