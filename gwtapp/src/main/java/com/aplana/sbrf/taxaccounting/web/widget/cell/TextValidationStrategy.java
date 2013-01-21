package com.aplana.sbrf.taxaccounting.web.widget.cell;

public class TextValidationStrategy implements ValidatedInputCell.ValidationStrategy {
	int maxLength;
	public TextValidationStrategy(int maxLength) {
		this.maxLength = maxLength;
	}
	@Override
	public boolean matches(String valueToCheck) {
		return valueToCheck.length() <= maxLength;
	}
}