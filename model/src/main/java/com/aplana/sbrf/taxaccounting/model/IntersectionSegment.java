package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * User: avanteev
 * Класс, представляющий собой описание пересечения для версий макетов
 */
public class IntersectionSegment implements Comparable {
    private Date beginDate;
    private Date endDate;
    private int templateId;
    private VersionedObjectStatus status;
    private int typeId;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

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
        IntersectionSegment intersection = (IntersectionSegment)o;
        int summand = 0;
        int equalSummand = 10;
        int result;
        if (endDate == null && intersection.getEndDate() != null){
            summand = 5;
            result = beginDate.compareTo(intersection.getBeginDate()) + 1 +
                    beginDate.compareTo(intersection.getEndDate()) + 1 + summand;
        }
        else if(endDate != null && intersection.getEndDate() == null){
            summand = -5;
            result = beginDate.compareTo(intersection.getBeginDate()) + (-1) +
                    (-1) + endDate.compareTo(intersection.getBeginDate()) + summand;
        } else if(endDate != null && intersection.getEndDate() != null){
            result = beginDate.compareTo(intersection.getBeginDate()) + endDate.compareTo(intersection.getEndDate()) +
                    beginDate.compareTo(intersection.getEndDate()) + endDate.compareTo(intersection.getBeginDate()) + summand;
        } else {
            result = beginDate.compareTo(intersection.getBeginDate());
        }

        if (beginDate.compareTo(intersection.getBeginDate()) == 0)
            result = result + (result>=0?equalSummand:-equalSummand);
        return result;
    }
}
