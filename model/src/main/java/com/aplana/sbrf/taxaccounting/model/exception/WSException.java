package com.aplana.sbrf.taxaccounting.model.exception;

public class WSException extends ServiceException {

	private static final long serialVersionUID = -1933146442508052101L;

	public enum SudirErrorCodes {
		
		SUDIR_000("SUDIR-000", "Unspecified error"),
		SUDIR_001("SUDIR-001", "Internal error"),
		SUDIR_002("SUDIR-002", "Unsupported operation"),
		SUDIR_003("SUDIR-003", "Access denied"),
		SUDIR_004("SUDIR-004", "Account not found"),
		SUDIR_005("SUDIR-005", "Account already exist"),
		SUDIR_006("SUDIR-006", "Invalid attribute format"),
		SUDIR_007("SUDIR-007", "Mandatory attribute not found"),
		SUDIR_008("SUDIR-008", "Illegal attribute value"),
		SUDIR_009("SUDIR-009", "Too many values for single valued attribute"),
		SUDIR_010("SUDIR-010", "Data type not supported"),
		SUDIR_011("SUDIR-011", "Аttribute parsing error");
		
		private String code;
		private String detailCode;

		SudirErrorCodes(String code, String detailCode){
			this.code = code;
			this.detailCode = detailCode;
		}
		
		public String detailCode() {
			return detailCode;
		};
		
		@Override
		public String toString() {
			return code;
		};
	}

	
	private SudirErrorCodes errorCode;

	public WSException() {
		super();
	}

	public WSException(SudirErrorCodes errorCode, String message, Object... params) {
		super(message, params);
		this.errorCode = errorCode;
	}

	public SudirErrorCodes getErrorCode() {
		return errorCode;
	}

}
