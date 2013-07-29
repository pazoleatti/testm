package com.aplana.sbrf.taxaccounting.web.module.periods.shared;


import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;

public class OpenException extends TaActionException {
	ErrorCode errorCode;
	String errorMsg;
	Integer reportPeriodId;

	public OpenException() {
	}

	public OpenException(ErrorCode errorCode, String errorMsg) {
		super(errorMsg);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}



	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Integer getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(Integer reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public enum ErrorCode {
		EXIST_OPEN,
		EXIST_CLOSED,
		PREVIOUS_ACTIVE;

	}
}


