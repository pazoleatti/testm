package com.aplana.sbrf.taxaccounting.web.widget.codemirror.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.editor.ui.client.adapters.HasTextEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

public class CodeMirror extends Composite implements HasChangeHandlers, IsEditor<LeafValueEditor<String>>,
        HasValue<String>, HasText {

    private LeafValueEditor<String> editor;

    @Override
    public LeafValueEditor<String> asEditor() {
        if (editor == null) {
            editor = HasTextEditor.of(this);
        }
        return editor;
    }

    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return addDomHandler(handler, ChangeEvent.getType());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    interface CodeMirrorUiBinder extends UiBinder<Panel, CodeMirror> {
	}

	private static CodeMirrorUiBinder ourUiBinder = GWT
			.create(CodeMirrorUiBinder.class);

	private CodeMirrorWrapper wrapper;

	@UiField
	FlowPanel content;

	@Override
	public void setValue(final String value) {
        setValue(value, false);
	}

    @Override
    public void setValue(String value, boolean fireEvents) {
        if (value != null) {
            wrapper.setValue(value);
        } else {
            wrapper.setValue("");
        }

        if (fireEvents){
            ValueChangeEvent.fire(this, getValue());
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
