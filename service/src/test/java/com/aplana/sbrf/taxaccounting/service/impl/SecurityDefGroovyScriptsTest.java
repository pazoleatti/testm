package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.service.impl.sandbox.ScriptSecureSandBoxWrapper;

public class SecurityDefGroovyScriptsTest {
	
	InputStream stream;
	private static String file = "accessFile.groovy";
	SecurityManager sm;
	ScriptEngine scriptEngine;
	ScriptEngine groovyScriptEngine;
	
	@Before
	public void init() throws MalformedURLException{
		File policyFile = new File(System.getProperty("user.home") + File.separator + ".java.policy");
		stream = SecurityDefGroovyScriptsTest.class.getResourceAsStream(file);
		System.out.println(System.getProperty("user.home") + File.separator + ".java.policy");
		ScriptEngineManager factory = new ScriptEngineManager();
		this.groovyScriptEngine = factory.getEngineByName("groovy");
		this.scriptEngine = new ScriptSecureSandBoxWrapper(groovyScriptEngine);
		if(System.getSecurityManager() == null && policyFile.exists()){
			System.out.println("Security manager not enabled, using default.");
			sm = new SecurityManager();
			System.setSecurityManager(sm);
		}
		else if(!policyFile.exists())
			System.out.println("Doesn't have local settings for security manager");
		else {
		      System.out.println("Security manager enabled.");
		}
	}
	
	@Test
	public void testWithSandBox() throws IOException, ScriptException{
		String accessScript = IOUtils.toString(stream, "UTF-8");
		scriptEngine.eval(accessScript);
		
	}
	
	@Test
	public void testWithoutSandBox() throws ScriptException, IOException{
		String accessScript = IOUtils.toString(stream, "UTF-8");
		groovyScriptEngine.eval(accessScript);
	}
	
	@After
	public void reset(){
		System.setSecurityManager(null);
		System.out.println(System.getSecurityManager());
	}

}
