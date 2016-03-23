package com.aplana.sbrf.taxaccounting.model;

import org.junit.Assert;
import org.junit.Test;

public class NumericColumnTest {
	@Test
	public void testFormatter() {
		NumericColumn numericColumn = new NumericColumn();

		Assert.assertEquals("12 345", numericColumn.getFormatter().format("12345"));
		Assert.assertEquals("123 456 789 012 345", numericColumn.getFormatter().format("123456789012345"));

		numericColumn.setPrecision(6);
		Assert.assertEquals("1 122 345 678 901.234500", numericColumn.getFormatter().format("1122345678901.2345"));
		Assert.assertEquals("-1 122 345 678 901.234500", numericColumn.getFormatter().format("-1122345678901.2345"));
		Assert.assertEquals("-122 345 678 901.234500", numericColumn.getFormatter().format("-122345678901.2345"));
		Assert.assertEquals("-122 345 678 901.234500", numericColumn.getFormatter().format("- 122345678901.2345"));

		numericColumn.setPrecision(2);
		Assert.assertEquals("-122 345 678 901.24", numericColumn.getFormatter().format("- 122345678901.2365"));
	}
}
