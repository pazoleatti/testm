package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class NotificationsFilterData implements Serializable {
    private static final long serialVersionUID = -2957561387544546546L;

    // если надо можно еще добавлять
    public enum SortColumn {
        DATE,
        TEXT
    }

	private Integer senderDepartmentId;
	private Integer receiverDepartmentId;

	private Integer countOfRecords;
	private Integer startIndex;

    private SortColumn sortColumn = SortColumn.DATE;
    private boolean isAsc = false;

	public Integer getSenderDepartmentId() {
		return senderDepartmentId;
	}

	public void setSenderDepartmentId(Integer senderDepartmentId) {
		this.senderDepartmentId = senderDepartmentId;
	}

	public Integer getReceiverDepartmentId() {
		return receiverDepartmentId;
	}

	public void setReceiverDepartmentId(Integer receiverDepartmentId) {
		this.receiverDepartmentId = receiverDepartmentId;
	}

	public Integer getCountOfRecords() {
		return countOfRecords;
	}

	public void setCountOfRecords(Integer countOfRecords) {
		this.countOfRecords = countOfRecords;
	}

	public Integer getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

    public SortColumn getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(SortColumn sortColumn) {
        this.sortColumn = sortColumn;
    }

    public boolean isAsc() {
        return isAsc;
    }

    public void setAsc(boolean isAsc) {
        this.isAsc = isAsc;
    }
}
