package com.aplana.sbrf.taxaccounting.model;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import org.junit.Test;

public class TaxTypeTest {
	@Test
	public void testCodes() {
		// Проверяем, что идентификаторы всех состояний уникальны
		Set<Character> codes = new HashSet<Character>();
		for (TaxType taxType: TaxType.values()) {
			char code = taxType.getCode();
			if (codes.contains(code)) {
				fail("Multiple occurences of code '" + code + "'");		
			}
			codes.add(code);
		}		
	}
	
	@Test
	public void testFromCode() {
		TaxType taxType = TaxType.fromCode('P');
		Assert.assertEquals(TaxType.PROPERTY, taxType);
		
		try {
			taxType = TaxType.fromCode('X');
			Assert.fail("Exception expected");
		} catch (IllegalArgumentException e) {
		}
	}

}
