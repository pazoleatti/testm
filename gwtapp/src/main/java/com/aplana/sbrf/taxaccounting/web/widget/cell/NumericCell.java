package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Window;

import static com.google.gwt.user.client.Event.sinkEvents;

public class NumericCell extends EditTextCell {

	InputElement inputField;

	@Override
	protected void edit(Context context, Element parent, String value) {
		super.edit(context, parent, value);
		inputField = parent.getFirstChild().<InputElement> cast();
		addCutHandler(inputField);
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
				addCutHandler(inputField);
			}
			if (!isWhatWeNeed(event)) {
				event.preventDefault();
				return;
			}
		}
	}

	/**
	 *
	 * @param event ивент для фильтрации
	 * @return true если этот ивент нам подходит false если нет
	 *
	 */
	private boolean isWhatWeNeed(NativeEvent event) {
		if(event!=null){
			if (((event.getKeyCode()>=48) && (event.getKeyCode()<=57)) // Number keys
					|| ((event.getKeyCode()>=96) && (event.getKeyCode()<=105)) //Num keys
					|| ((event.getKeyCode()==190) || (event.getKeyCode()==8) || (event.getKeyCode()==46) // backspace delete dot
					|| ((event.getKeyCode()>=37) && (event.getKeyCode()<=40)) )  //Arrow keys
					|| ((event.getKeyCode()==35) || (event.getKeyCode()==36)) // Home End
					|| ((event.getCtrlKey() && event.getKeyCode()==65)) // Ctrl+A
					|| ((event.getCtrlKey() && event.getKeyCode()==67)) // Ctrl+C
					|| ((event.getCtrlKey() && event.getKeyCode()==86)) // Ctrl+V
					|| ((event.getCtrlKey() && event.getKeyCode()==88)) // Ctrl+X
					|| (event.getKeyCode() == 189)                      // Minus
					|| (event.getKeyCode() == 109)                      // Num Minus
					|| (event.getKeyCode() == 110)                      // Num dot
				)
			{
				return true;
			}
		}
		return false;
	}

	private native void addCutHandler(Element elementID)
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

	public void handlePaste(String value) {
		final String oldValue = inputField.getValue();
		try {
			Double.parseDouble(value);
		} catch (NumberFormatException e) {
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					inputField.setValue(oldValue);
				}
			});
		}
	}
}
