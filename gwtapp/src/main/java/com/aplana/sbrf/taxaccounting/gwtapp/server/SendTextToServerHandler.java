package com.aplana.sbrf.taxaccounting.gwtapp.server;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.gwtapp.shared.FieldVerifier;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SendTextToServer;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SendTextToServerResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class SendTextToServerHandler extends
AbstractActionHandler<SendTextToServer, SendTextToServerResult> {

	@Autowired
	private ServletContext servletContext;

	public SendTextToServerHandler() {
		super(SendTextToServer.class);
	}

	@Override
	public SendTextToServerResult execute(SendTextToServer action,
			ExecutionContext context) throws ActionException {

		String input = action.getTextToServer();

		// Verify that the input is valid.
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new ActionException("Name must be at least 4 characters long");
		}

		String serverInfo = servletContext.getServerInfo();
		return new SendTextToServerResult("Hello, " + input
				+ "!<br><br>I am running " + serverInfo);
	}

	@Override
	public Class<SendTextToServer> getActionType() {
		return SendTextToServer.class;
	}

	@Override
	public void undo(SendTextToServer action, SendTextToServerResult result, ExecutionContext context) throws ActionException {
		// Not undoable
	}

}
