package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.gwtplatform.dispatch.shared.Result;
/**
 * 
 * @author Eugene Stetsenko
 * Результат запроса для получения доступных переходов между этапами.
 * Содержит доступные переходы между этапами.
 *
 */
public class GetAvailableMovesResult implements Result {
	private List<WorkflowMove> availableMoves;

	public List<WorkflowMove> getAvailableMoves() {
		return availableMoves;
	}

	public void setAvailableMoves(List<WorkflowMove> availableMoves) {
		this.availableMoves = availableMoves;
	}
	
	
}