package com.aplana.sbrf.taxaccounting.web.widget.codemirror;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;


public class CodeMirror extends Composite implements LeafValueEditor<String>, TakesValue<String> {
	interface CodeMirrorUiBinder extends UiBinder<HTMLPanel, CodeMirror> {
	}

	private static CodeMirrorUiBinder ourUiBinder = GWT.create(CodeMirrorUiBinder.class);

	private CodeMirrorWrapper wrapper;

	@UiField
	HTMLPanel content;

	@Override
	public void setValue(final String value) {
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				if (value != null) {
					wrapper.setValue(value);
				}
				else {
					wrapper.setValue("");
				}
				wrapper.clearHistory();
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
		config = config.setShowLineNumbers(true).setMatchBrackets(true).setIndentWithTabs(true);

		//then we bind code mirror to the text area
		wrapper = CodeMirrorWrapper.createEditor(content.getElement(), config);
	}

}
