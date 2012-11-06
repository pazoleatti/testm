package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * An action that can be sent using an {@link DispatchAsync} (client-side)
 * corresponding to a {@link com.gwtplatform.dispatch.server.Dispatch}
 * (server-side).
 */
public class SendTextToServer extends UnsecuredActionImpl<SendTextToServerResult> {

	private String textToServer;

	public SendTextToServer(final String textToServer) {
		this.textToServer = textToServer;
	}

	/**
	 * For serialization only.
	 */
	@SuppressWarnings("unused")
	private SendTextToServer() {
	}

	public String getTextToServer() {
		return textToServer;
	}
}
