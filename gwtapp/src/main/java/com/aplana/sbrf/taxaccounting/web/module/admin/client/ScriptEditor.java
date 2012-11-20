package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.Script;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

/**
 * @author Vitalii Samolovskikh
 */
public class ScriptEditor extends Composite implements Editor<Script> {
	interface MyUiBinder extends UiBinder<Widget, ScriptEditor> {}
	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	// Empty interface declaration, similar to UiBinder
	interface Driver extends SimpleBeanEditorDriver<Script, ScriptEditor> {}

	@UiField
	TextBox name;

	@UiField
	TextBox condition;

	@UiField
	@Path("rowScript")
	CheckBox perRow;

	@UiField
	TextArea body;

	private final Driver driver = GWT.create(Driver.class);

	public ScriptEditor() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	public void setValue(Script script){
		driver.edit(script);
	}

	public Script getValue(){
		return flush();
	}

	public Script flush() {
		return driver.flush();
	}
}
