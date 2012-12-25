package com.aplana.sbrf.taxaccounting.web.widget.cell;

public class RegularExpressionValidationStrategy implements MaskedTextInputCell.ValidationStrategy {
	private String regularExpression;
	public RegularExpressionValidationStrategy(String regularExpression) {
		this.regularExpression = regularExpression;
	}
	@Override
	public boolean matches(String valueToCheck) {
		return valueToCheck.matches(regularExpression);
	}
}