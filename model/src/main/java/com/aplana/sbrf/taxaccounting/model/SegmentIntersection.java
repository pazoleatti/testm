package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * User: avanteev
 */
public class SegmentIntersection implements Comparable {
    private Date beginDate;
    private Date endDate;
    private int templateId;
    private VersionedObjectStatus status;

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    @Override
    public int compareTo(Object o) {
        SegmentIntersection intersection = (SegmentIntersection)o;
        int summand = 0;
        if (endDate == null && intersection.getEndDate() != null){
            summand = 5;
            return beginDate.compareTo(intersection.getBeginDate()) + 1 +
                    beginDate.compareTo(intersection.getEndDate()) + 1 + summand;
        }
        else if(endDate != null && intersection.getEndDate() == null){
            summand = -5;
            return beginDate.compareTo(intersection.getBeginDate()) + (-1) +
                    (-1) + endDate.compareTo(intersection.getBeginDate()) + summand;
        } else if(endDate != null && intersection.getEndDate() != null){
            return beginDate.compareTo(intersection.getBeginDate()) + endDate.compareTo(intersection.getEndDate()) +
                    beginDate.compareTo(intersection.getEndDate()) + endDate.compareTo(intersection.getBeginDate()) + summand;
        } else {
            return beginDate.compareTo(intersection.getBeginDate());
        }
    }
}
