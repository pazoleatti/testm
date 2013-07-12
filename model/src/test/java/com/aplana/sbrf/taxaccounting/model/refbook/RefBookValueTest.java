package com.aplana.sbrf.taxaccounting.model.refbook;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Тест модельного класса RefBookValue
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 09.07.13 14:12
 */

public class RefBookValueTest {

	private static final String TEST_STRING = "Вася";
	private static final String WRONG_TEST_STRING = "Сергей";
	private static final Number TEST_NUMBER = 234.23;
	private static final Number WRONG_TEST_NUMBER = 9981;
	private static final Date TEST_DATE = new Date();
	private static final Date WRONG_TEST_DATE = new Date(325434534);
	private static final Long TEST_REF = 3L;
	private static final Long WRONG_TEST_REF = 9L;

	@Test
	public void testString() {
		RefBookValue value = new RefBookValue(RefBookAttributeType.STRING, TEST_STRING);
		Assert.assertEquals(TEST_STRING, value.getStringValue());
		Assert.assertNotEquals(WRONG_TEST_STRING, value.getStringValue());
		Assert.assertNull(value.getDateValue());
		Assert.assertNull(value.getNumberValue());
		Assert.assertNull(value.getReferenceValue());
		Assert.assertNull(value.getReferenceObject());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongType() {
		RefBookValue value = new RefBookValue(RefBookAttributeType.STRING, TEST_STRING);
		value.setValue(45.2);
	}

	@Test
	public void testNumber() {
		RefBookValue value = new RefBookValue(RefBookAttributeType.NUMBER, TEST_NUMBER);
		Assert.assertEquals(TEST_NUMBER, value.getNumberValue());
		Assert.assertNotEquals(WRONG_TEST_NUMBER, value.getNumberValue());
		Assert.assertNull(value.getDateValue());
		Assert.assertNull(value.getStringValue());
		Assert.assertNull(value.getReferenceValue());
		Assert.assertNull(value.getReferenceObject());
	}

	@Test
	public void testDate() {
		RefBookValue value = new RefBookValue(RefBookAttributeType.DATE, TEST_DATE);
		Assert.assertEquals(TEST_DATE, value.getDateValue());
		Assert.assertNotEquals(WRONG_TEST_DATE, value.getDateValue());
		Assert.assertNull(value.getNumberValue());
		Assert.assertNull(value.getStringValue());
		Assert.assertNull(value.getReferenceValue());
		Assert.assertNull(value.getReferenceObject());
	}

	@Test
	public void testNull() {
		RefBookValue value = new RefBookValue(RefBookAttributeType.DATE, null);
		Assert.assertNull(value.getDateValue());
		Assert.assertNull(value.getNumberValue());
		Assert.assertNull(value.getStringValue());
		Assert.assertNull(value.getReferenceValue());
		Assert.assertNull(value.getReferenceObject());
	}

	@Test
	public void testRef() {
		RefBookValue value = new RefBookValue(RefBookAttributeType.REFERENCE, TEST_REF);
		Assert.assertEquals(TEST_REF, value.getReferenceValue());
		Assert.assertNotEquals(WRONG_TEST_REF, value.getReferenceValue());
		Assert.assertNull(value.getNumberValue());
		Assert.assertNull(value.getStringValue());
		Assert.assertNull(value.getDateValue());
		Assert.assertNull(value.getReferenceObject());
	}

	@Test
	public void testRefObject() {
		Map<String, RefBookValue> testRef = new HashMap<String, RefBookValue>();
		testRef.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, TEST_REF));

		Map<String, RefBookValue> wrongTestRef = new HashMap<String, RefBookValue>();
		wrongTestRef.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, WRONG_TEST_REF));

		RefBookValue value = new RefBookValue(RefBookAttributeType.REFERENCE, testRef);
		Assert.assertEquals(testRef, value.getReferenceObject());
		Assert.assertNotEquals(wrongTestRef, value.getReferenceObject());
		Assert.assertNull(value.getNumberValue());
		Assert.assertNull(value.getStringValue());
		Assert.assertNull(value.getDateValue());
		Assert.assertNull(value.getReferenceValue());
	}
}