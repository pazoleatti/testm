package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.Result;


public class LoadRefBookResult implements Result {
	private static final long serialVersionUID = -8740180359930296291L;
	
	private List<LogEntry> entries;

	public List<LogEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<LogEntry> entries) {
		this.entries = entries;
	}




    
}
