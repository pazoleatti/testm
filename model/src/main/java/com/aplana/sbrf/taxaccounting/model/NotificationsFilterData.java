package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

public class NotificationsFilterData implements Serializable {
    private static final long serialVersionUID = -2957561387544546546L;

    // если надо можно еще добавлять
    public enum SortColumn {
        DATE,
        TEXT
    }

    /** Фильтр по подразделению-отправителю */
	private Integer senderDepartmentId;
    /** Фильтр по подразделению-получателю */
	private Integer receiverDepartmentId;
    /** Фильтр по конкретному пользователю, ожидающему уведомлений */
    private Integer userId;
    /** Фильтр по ролям пользователя */
    private List<Integer> userRoleIds;

	private Integer countOfRecords;
	private Integer startIndex;

    private SortColumn sortColumn = SortColumn.DATE;
    private boolean isAsc = false;

    public List<Integer> getUserRoleIds() {
        return userRoleIds;
    }

    public void setUserRoleIds(List<Integer> userRoleIds) {
        this.userRoleIds = userRoleIds;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

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
