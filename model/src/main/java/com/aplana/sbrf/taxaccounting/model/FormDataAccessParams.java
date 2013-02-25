package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Параметры доступа к объекту данных по {@link FormData налоговой форме}
 * @author DSultanbekov
 */
public class FormDataAccessParams implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean canRead;
	private boolean canEdit;
	private boolean canDelete;
	private List<WorkflowMove> availableWorkflowMoves;
	
	public boolean isCanRead() {
		return canRead;
	}
	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}
	public boolean isCanEdit() {
		return canEdit;
	}
	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}
	public boolean isCanDelete() {
		return canDelete;
	}
	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}
	public List<WorkflowMove> getAvailableWorkflowMoves() {
		return availableWorkflowMoves;
	}
	public void setAvailableWorkflowMoves(List<WorkflowMove> workflowMoves) {
		this.availableWorkflowMoves = workflowMoves;
	}

	@Override
	public String toString() {
		return "FormDataAccessParams{" +
				"canRead=" + canRead +
				", canEdit=" + canEdit +
				", canDelete=" + canDelete +
				", availableWorkflowMoves=" + availableWorkflowMoves +
				'}';
	}
}
