package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;

public class ValidatedInputCell extends KeyPressableTextInputCell {
	public interface ValidationStrategy {
		public boolean matches(String valueToCheck);
	}

	private ValidationStrategy overallFormValidationStrategy;
	private ValidationStrategy validKeystrokeValidationStrategy;

	public ValidatedInputCell(ValidationStrategy overallFormValidationStrategy, ValidationStrategy validKeystrokeValidationStrategy) {
		if (overallFormValidationStrategy != null) {
			this.overallFormValidationStrategy = overallFormValidationStrategy;
		} else {
			this.overallFormValidationStrategy = new DefaultValidationStrategy();
		}

		if (validKeystrokeValidationStrategy != null) {
			this.validKeystrokeValidationStrategy = validKeystrokeValidationStrategy;
		} else {
			this.validKeystrokeValidationStrategy = new DefaultValidationStrategy();
		}
	}

	@Override
	public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element parent, String value,
	                           NativeEvent event, ValueUpdater<String> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		addPasteHandler(getInputElement(parent));
		if ("keypress".equals(event.getType())) {
			if (event.getKeyCode() != KeyCodes.KEY_BACKSPACE
					&& (event.getKeyCode() != KeyCodes.KEY_DELETE || (char)event.getCharCode() == '.')
					&& event.getKeyCode() != KeyCodes.KEY_UP
					&& event.getKeyCode() != KeyCodes.KEY_DOWN
					&& event.getKeyCode() != KeyCodes.KEY_LEFT
					&& event.getKeyCode() != KeyCodes.KEY_RIGHT
					&& event.getKeyCode() != KeyCodes.KEY_HOME
					&& event.getKeyCode() != KeyCodes.KEY_END) {
				String keystroke = String.valueOf((char) event.getCharCode());
				String ourString = (new StringBuffer(getInputElement(parent).getValue())).insert(getPos(getInputElement(parent)), keystroke).toString();
				handleInvalidKeystroke(keystroke, event);
				handleInvalidOverallForm(ourString, event);
			}
		}
	}
	protected void handleInvalidOverallForm(String valueOfEntireField, NativeEvent event) {
		if (!overallFormValidationStrategy.matches(valueOfEntireField)) {
			event.preventDefault();
		}
	}
	protected void handleInvalidKeystroke(String keystroke, NativeEvent event) {
		if (!validKeystrokeValidationStrategy.matches(keystroke)) {
			event.preventDefault();
		}
	}

	private native int getPos(Element ctrl)
	/*-{
		var CaretPos = 0;
		if (document.selection) {
			ctrl.focus ();
			var Sel = document.selection.createRange ();
			Sel.moveStart ('character', -ctrl.value.length);
			CaretPos = Sel.text.length;
		}
		else if (ctrl.selectionStart || ctrl.selectionStart == '0') {
			CaretPos = ctrl.selectionStart;
		}
		return (CaretPos);

	}-*/;

	private native void addPasteHandler(Element elementID)
    /*-{
	    var temp = this;
	    var pastedText = undefined;
	    elementID.onpaste = function(e) {
		    if (window.clipboardData && window.clipboardData.getData) { // IE
			    pastedText = window.clipboardData.getData('Text');
		    } else if (e.clipboardData && e.clipboardData.getData) {
			    pastedText = e.clipboardData.getData('text/plain');
		    }
		    temp.@com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell::handlePaste(Ljava/lang/String;Lcom/google/gwt/dom/client/InputElement;)(pastedText, elementID);
	    }
    }-*/;

	private void handlePaste(String value, final InputElement input) {
		final String oldValue = input.getValue();
		if (!overallFormValidationStrategy.matches(oldValue+value)) {
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					input.setValue(oldValue);
				}
			});
		}
	}

}