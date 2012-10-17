package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.gwtplatform.mvp.client.ViewImpl;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FormDataListView extends ViewImpl implements FormDataListPresenter.MyView {

	private static final String html = "<h1>Web Application Starter Project</h1>\n"
			+ "<table align=\"center\">\n"
			+ "  <tr>\n"
			+ "    <td colspan=\"2\" style=\"font-weight:bold;\">Please enter your name:</td>\n"
			+ "  </tr>\n"
			+ "  <tr>\n"
			+ "    <td id=\"nameFieldContainer\"></td>\n"
			+ "    <td id=\"sendButtonContainer\"></td>\n"
			+ "  </tr>\n"
			+ "  <tr>\n"
			+ "    <td colspan=\"2\" style=\"color:red;\" id=\"errorLabelContainer\"></td>\n"
			+ "  </tr>\n" + "</table>\n";

	private final HTMLPanel panel = new HTMLPanel(html);

	private final Label errorLabel;
	private final TextBox nameField;
	private final Button sendButton;

	@Inject
	public FormDataListView() {
		sendButton = new Button("Send");
		nameField = new TextBox();
		nameField.setText("GWT User");
		errorLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		panel.add(nameField, "nameFieldContainer");
		panel.add(sendButton, "sendButtonContainer");
		panel.add(errorLabel, "errorLabelContainer");
	}

	@Override
	public Widget asWidget() {
		return panel;
	}

	@Override
	public String getName() {
		return nameField.getText();
	}

	@Override
	public Button getSendButton() {
		return sendButton;
	}

	@Override
	public void resetAndFocus() {
		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();
	}

	@Override
	public void setError(String errorText) {
		errorLabel.setText(errorText);
	}
}
