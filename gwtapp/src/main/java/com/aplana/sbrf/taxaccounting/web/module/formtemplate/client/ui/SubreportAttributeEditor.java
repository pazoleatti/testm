package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.*;


public class SubreportAttributeEditor extends Composite implements Editor<DeclarationSubreport>, TakesValue<DeclarationSubreport> {

    interface MyUiBinder extends UiBinder<Widget, SubreportAttributeEditor> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	// Empty interface declaration, similar to UiBinder
	interface Driver extends SimpleBeanEditorDriver<DeclarationSubreport, SubreportAttributeEditor> {
	}

	private boolean initialized = false;
	private final Driver driver = GWT.create(Driver.class);

	@UiField
	TextBox alias;

	@UiField
    TextBox name;

	public SubreportAttributeEditor() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	/**
	 * Устанавливает колонку для редактирования.
	 *
	 * @param subreport
	 */
	@Override
	public void setValue(DeclarationSubreport subreport) {
		driver.edit(subreport);
		initialized = subreport != null;
	}

	/**
	 * It's an alias of the <code>flush</code> method.
	 *
	 * @return merged column
	 */
	@Override
	public DeclarationSubreport getValue() {
		return flush();
	}

	public DeclarationSubreport flush() {
		if (initialized) {
			return driver.flush();
		} else {
			return null;
		}
	}

}