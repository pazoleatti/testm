package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * The result of a {@link SendTextToServer} action.
 */
public class SendTextToServerResult implements Result {

	private String response;

	public SendTextToServerResult(final String response) {
		this.response = response;
	}

	/**
	 * For serialization only.
	 */
	@SuppressWarnings("unused")
	private SendTextToServerResult() {
	}

	public String getResponse() {
		return response;
	}

}
