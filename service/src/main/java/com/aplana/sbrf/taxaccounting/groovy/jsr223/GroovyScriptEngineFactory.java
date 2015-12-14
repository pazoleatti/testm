package com.aplana.sbrf.taxaccounting.groovy.jsr223;

import javax.script.ScriptEngine;

/**
 * @author lhaziev
 */
public class GroovyScriptEngineFactory extends org.codehaus.groovy.jsr223.GroovyScriptEngineFactory {

	@Override
    public ScriptEngine getScriptEngine() {
        return new GroovyScriptEngine();
    }
}