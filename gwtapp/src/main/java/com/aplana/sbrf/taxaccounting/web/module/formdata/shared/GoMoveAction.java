package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
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
    private TaxType taxType;
    private boolean force;

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
    public TaxType getTaxType() {
        return taxType;
    }
    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
    public boolean isForce() {
        return force;
    }
    public void setForce(boolean force) {
        this.force = force;
    }

    @Override
	public String getName() {
		return "";
	}


}
