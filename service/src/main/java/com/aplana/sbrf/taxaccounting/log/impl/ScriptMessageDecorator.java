package com.aplana.sbrf.taxaccounting.log.impl;

import com.aplana.sbrf.taxaccounting.model.log.LogMessageDecorator;


public class ScriptMessageDecorator implements LogMessageDecorator {
	protected String scriptName;	
	
	public ScriptMessageDecorator(){
		super();
	}
	
	public ScriptMessageDecorator(String scriptName) {
		super();
		this.scriptName = scriptName;
	}

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
