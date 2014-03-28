package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
/**
 * 
 * @author Eugene Stetsenko
 * Запроса для перехода между этапами.
 *
 */
public class GoMoveAction extends UnsecuredActionImpl<GoMoveResult> implements ActionName {
	private long formDataId;
	private WorkflowMove move;
	private String reasonToWorkflowMove;

	public String getReasonToWorkflowMove() {
		return reasonToWorkflowMove;
	}

	public void setReasonToWorkflowMove(String reasonToWorkflowMove) {
		this.reasonToWorkflowMove = reasonToWorkflowMove;
	}

	public long getFormDataId() {
		return formDataId;
	}
	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}
	public WorkflowMove getMove() {
		return move;
	}
	public void setMove(WorkflowMove move) {
		this.move = move;
	}
	@Override
	public String getName() {
		return "Запроса для перехода между этапами";
	}


}
