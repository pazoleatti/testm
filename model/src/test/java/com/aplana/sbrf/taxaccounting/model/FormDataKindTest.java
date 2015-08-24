package com.aplana.sbrf.taxaccounting.model;

import static org.junit.Assert.assertEquals;
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
			ids.add(id);
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

        kind = FormDataKind.fromId(4);
        assert (kind == FormDataKind.UNP);

        kind = FormDataKind.fromId(5);
        assert (kind == FormDataKind.ADDITIONAL);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFromIdException() {
		FormDataKind.fromId(100);
	}

	@Test
	public void testGetName() {
		FormDataKind kind = FormDataKind.fromId(3);
		assertEquals("Сводная", kind.getTitle());
	}
}
