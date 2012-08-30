package com.aplana.sbrf.taxaccounting.dao;

/**
 * Проверка, которую нужно выполнить при проверке целостности данных в заполенной форме
 * Логика проверки реализуется в виде скрипта на языке Groovy
 */
public class RowCheck {
	private String name;
	private String script;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
}
