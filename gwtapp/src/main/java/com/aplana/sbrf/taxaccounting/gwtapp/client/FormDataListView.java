package com.aplana.sbrf.taxaccounting.gwtapp.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

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
			+ "  </tr>\n" 
			+ "  <tr>\n"
			+ "    <td colspan=\"2\" id=\"formDataListContainer\"></td>\n"
			+ "  </tr>\n" +  "</table>\n";

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

	@Override
	public void setFormDataList(List<FormData> records) {
		System.out.println("records size is: " + records.size());
		TextCell textCell = new TextCell();

	    // Create a CellList that uses the cell.
	    CellList<String> cellList = new CellList<String>(textCell);

	    // Set the total row count. This isn't strictly necessary, but it affects
	    // paging calculations, so its good habit to keep the row count up to date.
	    cellList.setRowCount(records.size(), true);

	    
	    List<String> strings = new ArrayList<String>(records.size());
	    for (FormData rec: records) {
	    	strings.add(rec.getForm().getType().getName() + " " + rec.getId());
	    }
	    // Push the data into the widget.
	    cellList.setRowData(0, strings);

	    // Add it to the root panel.
	    panel.add(cellList, "formDataListContainer");
	}
}
