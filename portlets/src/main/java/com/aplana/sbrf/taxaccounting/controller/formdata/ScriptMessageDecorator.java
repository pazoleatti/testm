package com.aplana.sbrf.taxaccounting.controller.formdata;

import com.aplana.sbrf.taxaccounting.log.LogMessageDecorator;

public class ScriptMessageDecorator implements LogMessageDecorator {
	protected String scriptName;	
	
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String operationName) {
		this.scriptName = operationName;
	}

	@Override
	public String getDecoratedMessage(String message) {
		return scriptName + ": " + message;
	}
}
