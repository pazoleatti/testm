package com.aplana.sbrf.taxaccounting.web.widget.codemirror.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;

public class CodeMirror extends Composite implements LeafValueEditor<String>,
		TakesValue<String>, HasText {
	interface CodeMirrorUiBinder extends UiBinder<Panel, CodeMirror> {
	}

	private static CodeMirrorUiBinder ourUiBinder = GWT
			.create(CodeMirrorUiBinder.class);

	private CodeMirrorWrapper wrapper;

	@UiField
	FlowPanel content;

	@Override
	public void setValue(final String value) {
		if (value != null) {
			wrapper.setValue(value);
		} else {
			wrapper.setValue("");
		}
		
		// Workaround
		// Нужно сделать рефрешь иначе компонент не отображает текст
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				wrapper.refresh();
			}
		});
	}

	@Override
	public String getValue() {
		return wrapper.getValue();
	}

	public CodeMirror() {
		initWidget(ourUiBinder.createAndBindUi(this));
		CodeMirrorConfig config = CodeMirrorConfig.makeBuilder();
		config = config.setShowLineNumbers(true).setMatchBrackets(true)
				.setIndentWithTabs(true);

		// then we bind code mirror to the text area
		
		wrapper = CodeMirrorWrapper.createEditor(content.getElement(), config);
	}

	@Override
	public String getText() {
		return getValue();
	}

	@Override
	public void setText(String text) {
		setValue(text);
	}

}
