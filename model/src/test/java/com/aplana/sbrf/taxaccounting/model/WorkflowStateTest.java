package com.aplana.sbrf.taxaccounting.model;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class WorkflowStateTest {
	@Test
	public void testIds() {
		// Проверяем, что идентификаторы всех состояний уникальны
		Set<Integer> ids = new HashSet<Integer>();
		for (WorkflowState state: WorkflowState.values()) {
			int id = state.getId();
			if (ids.contains(id)) {
				fail("Multiple occurences of id " + id);		
			}
			ids.add(id);
		}
	}
	
	@Test
	public void testFromId() {
		WorkflowState state = WorkflowState.fromId(1);
		assert(state == WorkflowState.CREATED);
		
		state = WorkflowState.fromId(2);
		assert(state == WorkflowState.PREPARED);
		
		state = WorkflowState.fromId(3);
		assert(state == WorkflowState.APPROVED);

		state = WorkflowState.fromId(4);
		assert(state == WorkflowState.ACCEPTED);

		// Проверка на несуществующий id
		try {
			state = WorkflowState.fromId(9);
			fail("Checking of wrong id retrieval failed");
		} catch (IllegalArgumentException e) {
			assert true;
		}
	}

}
