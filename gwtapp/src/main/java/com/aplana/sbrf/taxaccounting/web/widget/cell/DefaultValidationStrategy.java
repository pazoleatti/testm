package com.aplana.sbrf.taxaccounting.web.widget.cell;

public class DefaultValidationStrategy implements ValidatedInputCell.ValidationStrategy {
	@Override
	public boolean matches(String valueToCheck) {
		return true;
	}
}