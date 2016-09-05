package com.aplana.sbrf.taxaccounting.groovy.jsr223;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import groovy.lang.Tuple;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.util.ManagedConcurrentValueMap;
import org.codehaus.groovy.util.ReferenceBundle;

import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
    В отличаие от GroovyScriptEngineImpl при выполнении скрипта в globalClosures не добавляются public методы

    @author lhaziev
 */
public class GroovyScriptEngine extends GroovyScriptEngineImpl {

    private static boolean debug = false;

    // script-string-to-generated Class map
    private ManagedConcurrentValueMap<String, Class> classMap = new ManagedConcurrentValueMap<String, Class>(ReferenceBundle.getSoftBundle());
    // global closures map - this is used to simulate a single
    // global functions namespace
    private ManagedConcurrentValueMap<String, Closure> globalClosures = new ManagedConcurrentValueMap<String, Closure>(ReferenceBundle.getHardBundle());
    // lazily initialized factory
    private volatile GroovyScriptEngineFactory factory;

    // counter used to generate unique global Script class names
    private static int counter;

    static {
        counter = 0;
    }

    @Override
    public Object eval(Reader reader, ScriptContext ctx)
            throws ScriptException {
        return eval(readFully(reader), ctx);
    }

    @Override
    public Object eval(String script, ScriptContext ctx)
            throws ScriptException {
        try {
            String val = (String) ctx.getAttribute("#jsr223.groovy.engine.keep.globals", ScriptContext.ENGINE_SCOPE);
            ReferenceBundle bundle = ReferenceBundle.getHardBundle();
            if (val != null && val.length() > 0) {
                if (val.equalsIgnoreCase("soft")) {
                    bundle = ReferenceBundle.getSoftBundle();
                } else if (val.equalsIgnoreCase("weak")) {
                    bundle = ReferenceBundle.getWeakBundle();
                } else if (val.equalsIgnoreCase("phantom")) {
                    bundle = ReferenceBundle.getPhantomBundle();
                }
            }
            globalClosures.setBundle(bundle);
        } catch (ClassCastException cce) { /*ignore.*/ }

        try {
            Class clazz = getScriptClass(script);
            if (clazz == null) throw new ScriptException("Script class is null");
            return eval(clazz, ctx);
        } catch (SyntaxException e) {
            throw new ScriptException(e.getMessage(),
                    e.getSourceLocator(), e.getLine());
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            throw new ScriptException(e);
        }
    }

    // package-privates
    Object eval(Class scriptClass, final ScriptContext ctx) throws ScriptException {
        // Bindings so script has access to this environment.
        // Only initialize once.
        if (null == ctx.getAttribute("context", ScriptContext.ENGINE_SCOPE)) {
            // add context to bindings
            ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);

            // direct output to ctx.getWriter
            // If we're wrapping with a PrintWriter here,
            // enable autoFlush because otherwise it might not get done!
            final Writer writer = ctx.getWriter();
            ctx.setAttribute("out", (writer instanceof PrintWriter) ?
                            writer :
                            new PrintWriter(writer, true),
                    ScriptContext.ENGINE_SCOPE);

// Not going to do this after all (at least for now).
// Scripts can use context.{reader, writer, errorWriter}.
// That is a modern version of System.{in, out, err} or Console.{reader, writer}().
//
//            // New I/O names consistent with ScriptContext and java.io.Console.
//
//            ctx.setAttribute("writer", writer, ScriptContext.ENGINE_SCOPE);
//
//            // Direct errors to ctx.getErrorWriter
//            final Writer errorWriter = ctx.getErrorWriter();
//            ctx.setAttribute("errorWriter", (errorWriter instanceof PrintWriter) ?
//                                    errorWriter :
//                                    new PrintWriter(errorWriter),
//                                    ScriptContext.ENGINE_SCOPE);
//
//            // Get input from ctx.getReader
//            // We don't wrap with BufferedReader here because we expect that if
//            // the host wants that they do it.  Either way Groovy scripts will
//            // always have readLine because the GDK supplies it for Reader.
//            ctx.setAttribute("reader", ctx.getReader(), ScriptContext.ENGINE_SCOPE);
        }

        // Fix for GROOVY-3669: Can't use several times the same JSR-223 ScriptContext for differents groovy script
        if (ctx.getWriter() != null) {
            ctx.setAttribute("out", new PrintWriter(ctx.getWriter(), true), ScriptContext.ENGINE_SCOPE);
        }

        /*
         * We use the following Binding instance so that global variable lookup
         * will be done in the current ScriptContext instance.
         */
        Binding binding = new Binding(ctx.getBindings(ScriptContext.ENGINE_SCOPE)) {
            @Override
            public Object getVariable(String name) {
                synchronized (ctx) {
                    int scope = ctx.getAttributesScope(name);
                    if (scope != -1) {
                        return ctx.getAttribute(name, scope);
                    }
                }
                throw new MissingPropertyException(name, getClass());
            }

            @Override
            public void setVariable(String name, Object value) {
                synchronized (ctx) {
                    int scope = ctx.getAttributesScope(name);
                    if (scope == -1) {
                        scope = ScriptContext.ENGINE_SCOPE;
                    }
                    ctx.setAttribute(name, value, scope);
                }
            }
        };

        try {
            // if this class is not an instance of Script, it's a full-blown class
            // then simply return that class
            if (!Script.class.isAssignableFrom(scriptClass)) {
                return scriptClass;
            } else {
                // it's a script
                Script scriptObject = InvokerHelper.createScript(scriptClass, binding);

                MetaClass oldMetaClass = scriptObject.getMetaClass();

                /*
                * We override the MetaClass of this script object so that we can
                * forward calls to global closures (of previous or future "eval" calls)
                * This gives the illusion of working on the same "global" scope.
                */
                scriptObject.setMetaClass(new DelegatingMetaClass(oldMetaClass) {
                    @Override
                    public Object invokeMethod(Object object, String name, Object args) {
                        if (args == null) {
                            return invokeMethod(object, name, MetaClassHelper.EMPTY_ARRAY);
                        }
                        if (args instanceof Tuple) {
                            return invokeMethod(object, name, ((Tuple) args).toArray());
                        }
                        if (args instanceof Object[]) {
                            return invokeMethod(object, name, (Object[]) args);
                        } else {
                            return invokeMethod(object, name, new Object[]{args});
                        }
                    }

                    @Override
                    public Object invokeMethod(Object object, String name, Object[] args) {
                        try {
                            return super.invokeMethod(object, name, args);
                        } catch (MissingMethodException mme) {
                            return callGlobal(name, args, ctx);
                        }
                    }

                    @Override
                    public Object invokeStaticMethod(Object object, String name, Object[] args) {
                        try {
                            return super.invokeStaticMethod(object, name, args);
                        } catch (MissingMethodException mme) {
                            return callGlobal(name, args, ctx);
                        }
                    }
                });

                return scriptObject.run();
            }
        } catch (Exception e) {
            throw new ScriptException(e);
        } finally {
            // Fix for GROOVY-3669: Can't use several times the same JSR-223 ScriptContext for different groovy script
            // Groovy's scripting engine implementation adds those two variables in the binding
            // but should clean up afterwards
            ctx.removeAttribute("context", ScriptContext.ENGINE_SCOPE);
            ctx.removeAttribute("out", ScriptContext.ENGINE_SCOPE);
        }
    }

    Class getScriptClass(String script)
            throws SyntaxException,
            CompilationFailedException,
            IOException {
        Class clazz = classMap.get(script);
        if (clazz != null) {
            return clazz;
        }

        clazz = getClassLoader().parseClass(script, generateScriptName());
        classMap.put(script, clazz);
        return clazz;
    }

    //-- Internals only below this point

    private Object callGlobal(String name, Object[] args, ScriptContext ctx) {
        Closure closure = globalClosures.get(name);
        if (closure != null) {
            return closure.call(args);
        } else {
            // Look for closure valued variable in the
            // given ScriptContext. If available, call it.
            Object value = ctx.getAttribute(name);
            if (value instanceof Closure) {
                return ((Closure) value).call(args);
            } // else fall thru..
        }
        throw new MissingMethodException(name, getClass(), args);
    }

    // generate a unique name for top-level Script classes
    private synchronized String generateScriptName() {
        return "Script" + (++counter) + ".groovy";
    }


    private String readFully(Reader reader) throws ScriptException {
        char[] arr = new char[8 * 1024]; // 8K at a time
        StringBuilder buf = new StringBuilder();
        int numChars;
        try {
            while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
                buf.append(arr, 0, numChars);
            }
        } catch (IOException exp) {
            throw new ScriptException(exp);
        }
        return buf.toString();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        if (factory == null) {
            synchronized (this) {
                if (factory == null) {
                    factory = new GroovyScriptEngineFactory();
                }
            }
        }
        return factory;
    }
}