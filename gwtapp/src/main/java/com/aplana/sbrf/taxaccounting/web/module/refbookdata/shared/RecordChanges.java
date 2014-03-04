package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * Модель для информации об изменении записи
 * @author aivanov
 */
public class RecordChanges implements Serializable {

    private Long id;
    private Long parentId;
    private String name;
    private Date start;
    private Date end;

    public RecordChanges() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "RecordChanges{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
