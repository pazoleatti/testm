package com.aplana.sbrf.taxaccounting.model.source;

import java.io.Serializable;
import java.util.Date;

/**
 * Объект, включающий в себя данные по связке источников-приемников
 * @author dloshkarev
 */
public class SourceObject implements Serializable {
    private static final long serialVersionUID = -2718550955359013308L;

    /** Связка источник-приемник */
    private SourcePair sourcePair;
    /** Дата начала периода, на который будет действовать связка источников-приемников */
    private Date periodStart;
    /** Дата окончания периода, на который будет действовать связка источников-приемников */
    private Date periodEnd;

    public SourceObject(SourcePair sourcePair, Date periodStart, Date periodEnd) {
        this.sourcePair = sourcePair;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public SourceObject() {
    }

    public SourcePair getSourcePair() {
        return sourcePair;
    }

    public void setSourcePair(SourcePair sourcePair) {
        this.sourcePair = sourcePair;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceObject that = (SourceObject) o;

        if (periodEnd != null ? !periodEnd.equals(that.periodEnd) : that.periodEnd != null) return false;
        if (periodStart != null ? !periodStart.equals(that.periodStart) : that.periodStart != null) return false;
        if (sourcePair != null ? !sourcePair.equals(that.sourcePair) : that.sourcePair != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourcePair != null ? sourcePair.hashCode() : 0;
        result = 31 * result + (periodStart != null ? periodStart.hashCode() : 0);
        result = 31 * result + (periodEnd != null ? periodEnd.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SourceObject{" +
                "sourcePair=" + sourcePair +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                '}';
    }
}
