package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import java.io.Serializable;

public class AccessFlags implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8431612780564805182L;
	Boolean canRead;
	Boolean canEdit;
	Boolean canCreate;
	Boolean canDelete;
	
	public Boolean getCanRead() {
		return canRead;
	}
	public void setCanRead(Boolean canRead) {
		this.canRead = canRead;
	}
	public Boolean getCanEdit() {
		return canEdit;
	}
	public void setCanEdit(Boolean canEdit) {
		this.canEdit = canEdit;
	}
	public Boolean getCanCreate() {
		return canCreate;
	}
	public void setCanCreate(Boolean canCreate) {
		this.canCreate = canCreate;
	}
	public Boolean getCanDelete() {
		return canDelete;
	}
	public void setCanDelete(Boolean canDelete) {
		this.canDelete = canDelete;
	}
}
