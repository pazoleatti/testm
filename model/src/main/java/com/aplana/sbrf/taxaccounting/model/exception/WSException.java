package com.aplana.sbrf.taxaccounting.model.exception;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;

public class WSException extends ServiceException {

	private static final long serialVersionUID = -1933146442508052101L;

	public static enum SudirErrorCodes {
		
		SUDIR_000("SUDIR-000") {
			@Override
			public String detailCode() {
				return "Unspecified error";
			}
		},
		SUDIR_001("SUDIR-001") {
			@Override
			public String detailCode() {
				return "Internal error";
			}
		},
		SUDIR_002("SUDIR-002") {
			@Override
			public String detailCode() {
				return "Unsupported operation";
			}
		},
		SUDIR_003("SUDIR-003") {
			@Override
			public String detailCode() {
				return "Access denied";
			}
		},
		SUDIR_004("SUDIR-004") {
			@Override
			public String detailCode() {
				return "Account not found";
			}
		},
		SUDIR_005("SUDIR-005") {
			@Override
			public String detailCode() {
				return "Account already exist";
			}
		},
		SUDIR_006("SUDIR-006") {
			@Override
			public String detailCode() {
				return "Invalid attribute format";
			}
		},
		SUDIR_007("SUDIR-007") {
			@Override
			public String detailCode() {
				return "Mandatory attribute not found ";
			}
		},
		SUDIR_008("SUDIR-008") {
			@Override
			public String detailCode() {
				return "Illegal attribute value";
			}
		},
		SUDIR_009("SUDIR-009") {
			@Override
			public String detailCode() {
				return "Too many values for single valued attribute";
			}
		},
		SUDIR_010("SUDIR-010") {
			@Override
			public String detailCode() {
				return "Data type not supported";
			}
		},
		SUDIR_011("SUDIR-011") {
			@Override
			public String detailCode() {
				return "Аttribute parsing error";
			}
		};
		
		private String code;
		SudirErrorCodes(String code){this.code = code;}
		
		public abstract String detailCode();
		
		@Override
		public String toString() {
			return code;
		};
	}

	
	private SudirErrorCodes errorCode;

	public WSException(SudirErrorCodes errorCode, String message, Object... params) {
		super(message, params);
		this.errorCode = errorCode;
	}

	public SudirErrorCodes getErrorCode() {
		return errorCode;
	}

}
