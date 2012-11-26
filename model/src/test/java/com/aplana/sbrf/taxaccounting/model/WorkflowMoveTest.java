package com.aplana.sbrf.taxaccounting.model;

import org.junit.Test;

/**
 * Tests for WorkflowMove.
 *
 * @author Vitalii Samolovskikh
 */
public class WorkflowMoveTest {
	/**
	 * Test joining between <code>WorkflowMove</code> and <code>FormDataEvent</code>.
	 *
	 * Event must be unique for each move.
	 */
	@Test
	public void testEvent() {
		for (WorkflowMove wm1 : WorkflowMove.values()) {
			FormDataEvent fde1 = wm1.getEvent();
			for (WorkflowMove wm2 : WorkflowMove.values()) {
				FormDataEvent fde2 = wm2.getEvent();
				assert (wm1 == wm2 && fde1 == fde2) || (wm1 != wm2 && fde1 != fde2);
			}
		}
	}
}
