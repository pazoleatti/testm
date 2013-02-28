package com.aplana.sbrf.taxaccounting.service.impl.sandbox;

import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.security.GroovyCodeSourcePermission;

import java.io.FilePermission;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.BasicPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.PropertyPermission;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;

public class ScriptSecureSandBoxWrapper implements ScriptEngine {
	
	private ScriptEngine scriptEngine;
	private AccessControlContext accessControlContext;
	private String projector;
	
	public ScriptSecureSandBoxWrapper(){
		
	}
	
	public ScriptSecureSandBoxWrapper(ScriptEngine scriptEngine) throws MalformedURLException {
		this.scriptEngine = scriptEngine;
		this.projector = "file:/home/avanteev/iask/iask-acctax/trunk/workspace/SBRFACCTAX/taxaccounting/service/target/classes/";
		setPermissions(null);
	}
	

	@Override
	public Bindings createBindings() {
		return scriptEngine.createBindings();
	}

	@Override
	public Object eval(final String script) throws ScriptException {
		SecurityManager sm = System.getSecurityManager();
		AccessControlContext acc;
		if(sm!=null){
			acc = (AccessControlContext)sm.getSecurityContext();
			System.out.println(acc);
		}
		return AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Object run() {
				/*try {
					AccessControlContext acc = AccessController.getContext();
					return scriptEngine.eval(script);
				} catch (ScriptException e) {
					e.printStackTrace();
				}
				return null;*/
				GroovyCodeSource groovyCodeSource = new GroovyCodeSource(script, "accessScript", "/restrictedScript");
				return new GroovyShell().evaluate(groovyCodeSource);
			}
		},accessControlContext);
	}

	@Override
	public Object eval(Reader reader) throws ScriptException {
		return scriptEngine.eval(reader);
	}

	@Override
	public Object eval(String script, ScriptContext context)
			throws ScriptException {
		return scriptEngine.eval(script, context);
	}

	@Override
	public Object eval(Reader reader, ScriptContext context)
			throws ScriptException {
		return scriptEngine.eval(reader, context);
	}

	@Override
	public Object eval(final String script, final Bindings n) throws ScriptException {
		return AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Object run() {
				try {
					return scriptEngine.eval(script, n);
				} catch (ScriptException e) {
					throw new RuntimeException(e);
				}
			}
		}, accessControlContext);
	}

	@Override
	public Object eval(Reader reader, Bindings n) throws ScriptException {
		return scriptEngine.eval(reader, n);
	}

	@Override
	public Object get(String key) {
		return scriptEngine.get(key);
	}

	@Override
	public Bindings getBindings(int scope) {
		return scriptEngine.getBindings(scope);
	}

	@Override
	public ScriptContext getContext() {
		return scriptEngine.getContext();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return scriptEngine.getFactory();
	}

	@Override
	public void put(String key, Object value) {
		scriptEngine.put(key, value);
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {
		scriptEngine.setBindings(bindings, scope);
	}

	@Override
	public void setContext(ScriptContext context) {
		scriptEngine.setContext(context);
	}
	
	public void setPermissions(Collection<Permission> permissionCollection) throws MalformedURLException{
		Permissions perm = new Permissions();
		Permissions perm1 = new Permissions();
		Permissions perm2 = new Permissions();
		//perm.add(new PropertyPermission("file.encoding","read"));
		//perm.add(new FilePermission("/*", "read"));
		//perm.add(new GroovyCodeSourcePermission("/groovy/script"));
		if(permissionCollection != null){
			for (Permission p : permissionCollection) {
	//			perm.add(p);
			}
		}
		
		//perm.add(new AllPermission());
		/*perm.add(new GroovyCodeSourcePermission("/groovy/script"));
		perm1.add(new FilePermission("/tmp/*", "read"));*/
		/*perm1.add(new PropertyPermission("file.encoding","read"));
		perm1.add(new GroovyCodeSourcePermission("/groovy/script"));*/
		/*perm2.add(new AllPermission());
		perm2.add(new GroovyCodeSourcePermission("/restrictedScript"));*/
		
		System.out.println(ScriptSecureSandBoxWrapper.class.getProtectionDomain().getCodeSource().getLocation().toString() + ScriptSecureSandBoxWrapper.class.getSimpleName());
		ProtectionDomain protectionDomain = new ProtectionDomain(new CodeSource(
				new URL(ScriptSecureSandBoxWrapper.class.getProtectionDomain().getCodeSource().getLocation().toString() + 
						ScriptSecureSandBoxWrapper.class.getSimpleName() + ".class"), 
						(Certificate[])null), 
					perm);
		/*ProtectionDomain protectionDomain1 = new ProtectionDomain(
				new CodeSource(new URL("file:/home/avanteev/iask/iask-acctax/trunk/workspace/SBRFACCTAX/taxaccounting/service/target/test-classes/"), 
						(Certificate[])null), 
					perm1);*/
		/*ProtectionDomain protectionDomain2 = new ProtectionDomain(new CodeSource(new URL("file:/restrictedScript"), 
				(Certificate[])null), 
				perm2);*/
		
		accessControlContext = new AccessControlContext(
				new ProtectionDomain[]{protectionDomain});
		AccessControlContext acc = (AccessControlContext)AccessController.getContext();
		//AccessController.checkPermission(new GroovyCodeSourcePermission("/groovy/script"));
		//accessControlContext.checkPermission(new GroovyCodeSourcePermission("/groovy/script"));
		
		
	}

}
