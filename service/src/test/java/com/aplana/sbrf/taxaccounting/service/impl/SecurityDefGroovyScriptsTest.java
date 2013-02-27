package com.aplana.sbrf.taxaccounting.service.impl;

import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.security.AccessControlException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.service.impl.sandbox.ScriptSecureSandBoxWrapper;

public class SecurityDefGroovyScriptsTest {
	
	InputStream stream;
	private static String file = "accessFile.groovy";
	SecurityManager sm;
	ScriptEngine scriptEngine;
	
	@Before
	public void init() throws MalformedURLException{
		/*System.out.println(SecurityDefGroovyScriptsTest.class.getResource(file));
		stream = SecurityDefGroovyScriptsTest.class.getResourceAsStream(file);
		if(System.getSecurityManager() == null){
			System.out.println("Security manager not enabled, using default.");
			sm = new SecurityManager();
			System.setSecurityManager(sm);
		}
		else {
		      System.out.println("Security manager enabled.");
		}*/
		
		stream = SecurityDefGroovyScriptsTest.class.getResourceAsStream(file);
		System.out.println(stream);
		ScriptEngineManager factory = new ScriptEngineManager();
		this.scriptEngine = new ScriptSecureSandBoxWrapper(factory.getEngineByName("groovy"));
		if(System.getSecurityManager() == null){
			System.out.println("Security manager not enabled, using default.");
			sm = new SecurityManager();
			System.setSecurityManager(sm);
		}
		else {
		      System.out.println("Security manager enabled.");
		}
	}
	
	@Test(expected=AccessControlException.class)
	public void test() throws IOException, ScriptException{
		/*sm.checkPermission(new FilePermission("/tmp/1", "read,write,delete"));
		System.out.println(stream);
		String accessScript = IOUtils.toString(stream, "UTF-8");
		System.out.println(accessScript);
		GroovyCodeSource groovyCodeSource = new GroovyCodeSource(accessScript, "accessScript", "/restrictedScript");
		System.out.println(groovyCodeSource);
		new GroovyShell().evaluate(groovyCodeSource);*/
		String accessScript = IOUtils.toString(stream, "UTF-8");
		scriptEngine.eval(accessScript);
		

	}

}
