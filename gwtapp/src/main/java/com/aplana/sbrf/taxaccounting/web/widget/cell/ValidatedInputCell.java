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
	protected boolean checkInputtedValue(String value) {
		return overallFormValidationStrategy.matches(value);
	}
}