package com.aplana.sbrf.taxaccounting.web.widget.cell;

public class NumberValidationStrategy implements ValidatedInputCell.ValidationStrategy {
	int precision;
	public NumberValidationStrategy(int precision) {
		this.precision = precision;
	}
	@Override
	public boolean matches(String valueToCheck) {
		if (valueToCheck.contains("d")
				|| valueToCheck.contains("f")
				|| valueToCheck.contains(" ")) {
			return false;
		}
		if (valueToCheck.isEmpty()) {
			return true;
		} else if ((precision == 0) && valueToCheck.contains(".")) {
			return false;
		} else if (valueToCheck.contains(".") && valueToCheck.substring(valueToCheck.indexOf('.')).length() > precision + 1) {
			return false;
		} else if (valueToCheck.equals('-')) {
			return true;
		} else {
			try {
				Double.parseDouble(valueToCheck);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
}