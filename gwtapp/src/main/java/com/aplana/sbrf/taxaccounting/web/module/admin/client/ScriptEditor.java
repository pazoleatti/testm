package com.aplana.sbrf.taxaccounting.web.module.admin.client;

import com.aplana.sbrf.taxaccounting.model.Script;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.*;

/**
 * The editor for all scripts. You can use it in UiBinder. Just add schema
 * <p/>
 * <pre>xmlns:u="urn:import:com.aplana.sbrf.taxaccounting.web.module.admin.client"</pre>
 * <p/>
 * <pre>&lt;u:ScriptEditor ui:field="scriptEditor"/&gt;</pre>
 * <p/>
 * You also can use it as widget.
 * <p/>
 * You can set and get value, but when you call <code>getValue</code> it merges changes to bean.
 *
 * @author Vitalii Samolovskikh
 */
public class ScriptEditor extends Composite implements Editor<Script>, TakesValue<Script> {
	interface MyUiBinder extends UiBinder<Widget, ScriptEditor> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	// Empty interface declaration, similar to UiBinder
	interface Driver extends SimpleBeanEditorDriver<Script, ScriptEditor> {
	}

	private final Driver driver = GWT.create(Driver.class);

	@UiField
	TextBox name;

	@UiField
	TextBox condition;

	@UiField
	@Path("rowScript")
	CheckBox perRow;

	@UiField
	TextArea body;

	private boolean initialized = false;

	/**
	 * Constructs new editor.
	 */
	public ScriptEditor() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	/**
	 * Sets script bean value.
	 *
	 * @param script script for edit
	 */
	@Override
	public void setValue(Script script) {
		driver.edit(script);
		initialized = true;
	}

	/**
	 * It'a alias for <code>flush</code> method.
	 *
	 * @return merged script
	 */
	@Override
	public Script getValue() {
		return flush();
	}

	/**
	 * Merges changes to bean and returns bean.
	 *
	 * @return merged script
	 */
	public Script flush() {
		if (initialized) {
			return driver.flush();
		} else {
			return null;
		}
	}
}
