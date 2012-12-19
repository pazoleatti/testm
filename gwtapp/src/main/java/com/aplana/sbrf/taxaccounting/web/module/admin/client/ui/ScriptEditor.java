package com.aplana.sbrf.taxaccounting.web.module.admin.client.ui;

import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.web.widget.codemirror.CodeMirror;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.*;

/**
 * Элемент редактирования срипта. Его можно использовать в XML, только нужно добавить схему:
 * <p/>
 * <pre>xmlns:u="urn:import:com.aplana.sbrf.taxaccounting.web.module.admin.client"</pre>
 * <p/>
 * <pre>&lt;u:ScriptEditor ui:field="scriptEditor"/&gt;</pre>
 * <p/>
 * Это обычный виджет со значением. Можно устанавливать и получать значения. Только нужно понимать,
 * что при получении значения, мы получим не новое значение, а то же самое, с внесенными изменениями.
 * Т.е. если мы используем скрипт где-то еще, там он тоже изменится.
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
	TextArea condition;

	@UiField
	@Path("rowScript")
	CheckBox perRow;

	@UiField
	CodeMirror body;

	/**
	 * Флаг того, что элемент проинициализирован.
	 * Это нужно для того чтобы проверять, было ли установлено значение элемента или нет.
	 */
	private boolean initialized = false;

	/**
	 * Создает новый элемент для редактирования скриптов.
	 */
	public ScriptEditor() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		driver.initialize(this);
	}

	/**
	 * Устанавливает скрипт для редактирования.
	 *
	 * @param script скрипт
	 */
	@Override
	public void setValue(Script script) {
		driver.edit(script);
		initialized = script != null;
	}

	/**
	 * It's an alias of the <code>flush</code> method.
	 *
	 * @return merged script
	 */
	@Override
	public Script getValue() {
		return flush();
	}

	/**
	 * Мержит старые значения скрипта вместе со значениями из элементов формы.
	 * И возвращает скрипт.
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
