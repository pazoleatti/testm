package com.aplana.sbrf.taxaccounting.model;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class FormDataKindTest {
	@Test
	public void testIds() {
		// Проверяем, что идентификаторы всех состояний уникальны
		Set<Integer> ids = new HashSet<Integer>();
		for (FormDataKind kind: FormDataKind.values()) {
			int id = kind.getId();
			if (ids.contains(id)) {
				fail("Multiple occurences of id " + id);		
			}
		}		
	}
	
	@Test
	public void testFromId() {
		FormDataKind kind = FormDataKind.fromId(1);
		assert(kind == FormDataKind.PRIMARY);
		
		kind = FormDataKind.fromId(2);
		assert(kind == FormDataKind.CONSOLIDATED);
		
		kind = FormDataKind.fromId(3);
		assert(kind == FormDataKind.SUMMARY);
		
		try {
			kind = FormDataKind.fromId(100);
			fail("Checking of wrong id retrieval failed");
		} catch (IllegalArgumentException e) {
			
		}
	}
}
