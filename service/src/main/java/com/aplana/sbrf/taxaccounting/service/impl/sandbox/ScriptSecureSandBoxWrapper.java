package com.aplana.sbrf.taxaccounting.service.impl.sandbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.*;
import java.io.FilePermission;
import java.io.Reader;
import java.lang.reflect.ReflectPermission;
import java.net.MalformedURLException;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

public class ScriptSecureSandBoxWrapper implements ScriptEngine {

	private static final Log LOG = LogFactory.getLog(ScriptSecureSandBoxWrapper.class);

	private ScriptEngine scriptEngine;
	private AccessControlContext accessControlContext;
	
	public ScriptSecureSandBoxWrapper(){
	}
	
	public ScriptSecureSandBoxWrapper(ScriptEngine scriptEngine) throws MalformedURLException {
		this.scriptEngine = scriptEngine;
		setPermissions(null);
	}

	@Override
	public Bindings createBindings() {
		return scriptEngine.createBindings();
	}

	@Override
	public Object eval(final String script) throws ScriptException {
		final SecurityManager sm = System.getSecurityManager();
		AccessControlContext acc = null;
		if(sm!=null){
			acc = (AccessControlContext)sm.getSecurityContext();
			LOG.debug(acc);
		}
		return AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Object run() {
				try {
					return scriptEngine.eval(script);
				} catch (ScriptException e) {
					LOG.error(e.getMessage(), e);
				}
				return null;
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
	
	public final void setPermissions(Collection<Permission> permissionCollection) throws MalformedURLException{
		Permissions perm = new Permissions();
		if(permissionCollection != null){
			for (Permission p : permissionCollection) {
				perm.add(p);
			}
		}

		perm.add(new RuntimePermission("accessDeclaredMembers"));
		perm.add(new FilePermission("<<ALL FILES>>", "read,write"));
		perm.add(new PropertyPermission("ANTLR_DO_NOT_EXIT", "read"));
		perm.add(new PropertyPermission("ANTLR_USE_DIRECT_CLASS_LOADING", "read"));
		perm.add(new PropertyPermission("line.separator", "read"));
		perm.add(new LoggingPermission("control", null));
		perm.add(new RuntimePermission("setSecurityManager"));
		perm.add(new ReflectPermission("suppressAccessChecks"));
		perm.add(new PropertyPermission("cglib.debugLocation", "read"));
		perm.add(new RuntimePermission("getProtectionDomain"));
		perm.add(new PropertyPermission("guice.allow.nulls.bad.bad.bad", "read"));
		perm.add(new RuntimePermission("createClassLoader"));
		perm.add(new PropertyPermission("groovyjarjarantlr.ast", "read"));
		perm.add(new PropertyPermission("groovy.ast", "read"));
		perm.add(new RuntimePermission("setContextClassLoader"));
		
		ProtectionDomain protectionDomain = new ProtectionDomain(new CodeSource(
				null, 
				(Certificate[])null), 
				perm);
		
		accessControlContext = new AccessControlContext(
				new ProtectionDomain[]{protectionDomain});
	}
}
