package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.*;


public class ColumnAttributeEditor extends Composite implements Editor<Column>, TakesValue<Column>, HasEnabled {
    @Override
    public boolean isEnabled() {
        return alias.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        alias.setEnabled(enabled);
    }

    interface MyUiBinder extends UiBinder<Widget, ColumnAttributeEditor> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	// Empty interface declaration, similar to UiBinder
	interface Driver extends SimpleBeanEditorDriver<Column, ColumnAttributeEditor> {
	}

	private boolean initialized = false;
	private final Driver driver = GWT.create(Driver.class);

    @UiField
    TextBox shortName;

	@UiField
	TextBox alias;

	@UiField
	IntegerBox width;

	@UiField
	CheckBox checking;

	public ColumnAttributeEditor() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	/**
	 * Устанавливает колонку для редактирования.
	 *
	 * @param column
	 */
	@Override
	public void setValue(Column column) {
		driver.edit(column);
		initialized = column != null;
	}

	/**
	 * It's an alias of the <code>flush</code> method.
	 *
	 * @return merged column
	 */
	@Override
	public Column getValue() {
		return flush();
	}

	public Column flush() {
		if (initialized) {
			return driver.flush();
		} else {
			return null;
		}
	}

}