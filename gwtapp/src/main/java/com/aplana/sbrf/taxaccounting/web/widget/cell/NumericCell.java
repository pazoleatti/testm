package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Window;

// TODO: по возможности нужно порефакторить этот класс или найти лучшую реализацию
public class NumericCell extends EditTextCell {

	private InputElement inputField;
	private int precision;

	public NumericCell(int precision) {
		this.precision = precision;
	}
	@Override
	protected void edit(Context context, Element parent, String value) {
		super.edit(context, parent, value);
		inputField = parent.getFirstChild().<InputElement> cast();
		addPasteHandler(inputField);
	}
	@Override
	public void onBrowserEvent(Context context, final Element parent,
	                           final String value, final NativeEvent event,
	                           ValueUpdater<String> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		boolean isEditing = isEditing(context, parent, value);

		if(isEditing){
			if (Window.Navigator.getUserAgent().contains("MSIE")) {
				inputField = parent.getFirstChild().<InputElement> cast();
				addPasteHandler(inputField);
			}
			if ("keydown".equals(event.getType())) {
				// Проверяем подходящий ли символ нажат. Дополнительно проверяем корректность для видимых символов
				if (isWhatWeNeed(event)) {
					String ourString = (new StringBuffer(inputField.getValue())).insert(getPos(inputField), convertKeyCodeToChar(event.getKeyCode())).toString();
					if (isSymbolValue(event.getKeyCode()) && !isCorrectValue(ourString)) {
						event.preventDefault();
						return;
					}
				} else {
					event.preventDefault();
					return;
				}
			}
		}
	}

	private char convertKeyCodeToChar(int keyCode) {
		char inputtedChar;
		if ((keyCode==46) || (keyCode==110)) {
			inputtedChar = '.';
		} else if ((keyCode==189) || (keyCode==109)) {
			inputtedChar = '-';
		} else if ((keyCode>=96) && (keyCode<=105)) {
			keyCode-=48;
			inputtedChar = (char)keyCode;
		} else{
			inputtedChar = (char)keyCode;
		}
		return inputtedChar;
	}

	/**
	 *
	 * @param event ивент для фильтрации
	 * @return true если этот ивент нам подходит false если нет
	 *
	 */
	private boolean isWhatWeNeed(NativeEvent event) {
		if(event!=null){
			if (
				(event.getKeyCode()== KeyCodes.KEY_BACKSPACE)
					|| (event.getKeyCode()==KeyCodes.KEY_DELETE)
					|| (event.getKeyCode()==KeyCodes.KEY_HOME)
					|| (event.getKeyCode()==KeyCodes.KEY_END) // Home End
					|| (event.getKeyCode()==KeyCodes.KEY_LEFT)
					|| (event.getKeyCode()==KeyCodes.KEY_RIGHT)   //Arrow keys
					|| (event.getKeyCode()==KeyCodes.KEY_UP)
					|| (event.getKeyCode()==KeyCodes.KEY_DOWN)   //Arrow keys
					|| ((event.getCtrlKey() && event.getKeyCode()==65)) // Ctrl+A
					|| ((event.getCtrlKey() && event.getKeyCode()==67)) // Ctrl+C
					|| ((event.getCtrlKey() && event.getKeyCode()==86)) // Ctrl+V
					|| ((event.getCtrlKey() && event.getKeyCode()==88)) // Ctrl+X
					|| (isSymbolValue(event.getKeyCode()) && !event.getShiftKey())

				)
			{
				return true;
			}
		}
		return false;
	}

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
		    temp.@com.aplana.sbrf.taxaccounting.web.widget.cell.NumericCell::handlePaste(Ljava/lang/String;)(pastedText);
	    }
    }-*/;

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

	private void handlePaste(String value) {
		final String oldValue = inputField.getValue();
		if (!isCorrectValue(value)) {
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					inputField.setValue(oldValue);
				}
			});
		}
	}

	private boolean isSymbolValue(int value) {
		if (
			(value == 189)                      // Minus
				|| (value == 109)               // Num Minus
				|| (value==46)                  // dot
				|| (value == 110)               // Num dot
				|| ((value>=48) && (value<=57)) // Number keys
				|| ((value>=96) && (value<=105))// Num keys

		) {
			return true;
		}
		return false;
	}

	private boolean isCorrectValue(String value) {

		if ((precision == 0) && value.contains(".")) {
			return false;
		} else if (value.contains(".") && value.substring(value.indexOf(".")).length() > precision+1) {
			return false;
		} else if (value.equals("-")) {
			return true;
		} else {
			try {
				Double.parseDouble(value);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
}
