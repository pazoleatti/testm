package com.aplana.sbrf.taxaccounting.model;

public class NotificationsFilterData {

	private Integer senderDepartmentId;
	private Integer receiverDepartmentId;

	Integer countOfRecords;
	Integer startIndex;

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
}
