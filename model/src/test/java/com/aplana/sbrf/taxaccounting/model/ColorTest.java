package com.aplana.sbrf.taxaccounting.model;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ColorTest {
	@Test
	public void testIds() {
		// Проверяем, что идентификаторы всех состояний уникальны
		Set<Integer> ids = new HashSet<Integer>();
		for (Color color: Color.values()) {
			int id = color.getId();
			if (ids.contains(id)) {
				fail("Multiple occurences of id " + id);		
			}
			ids.add(id);
		}		
	}
	
	@Test
	public void testFromId() {
		Color color = Color.getById(3);
		assert(color == Color.LIGHT_BLUE);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromIdWrong() {
		Color.getById(999);
	}
	

}
