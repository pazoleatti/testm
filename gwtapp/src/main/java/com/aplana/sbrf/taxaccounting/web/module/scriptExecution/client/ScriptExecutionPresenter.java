package com.aplana.sbrf.taxaccounting.web.module.scriptExecution.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.scriptExecution.shared.ScriptExecutionAction;
import com.aplana.sbrf.taxaccounting.web.module.scriptExecution.shared.ScriptExecutionResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * @author Stanislav Yasinskiy
 */
public class ScriptExecutionPresenter extends Presenter<ScriptExecutionPresenter.MyView, ScriptExecutionPresenter.MyProxy>
        implements ScriptExecutionUiHandlers {

    private final DispatchAsync dispatchAsync;

    @Inject
    public ScriptExecutionPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatchAsync) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().setScriptCode("import groovy.transform.Field\n" +
                "import org.springframework.jndi.JndiTemplate\n" +
                "\n" +
                "import javax.sql.DataSource\n" +
                "\n" +
                "@Field\n" +
                "def dataSourceName = 'java:comp/env/jdbc/TaxAccDS'\n" +
                "\n" +
                "try {\n" +
                "    def template = new JndiTemplate()\n" +
                "    def DataSource dataSource = template.lookup(dataSourceName)\n" +
                "    def connection = dataSource.connection\n" +
                "\n" +
                "    def stmt = connection.createStatement()\n" +
                "\n" +
                "    def result = 0\n" +
                "\n" +
                "    result = stmt.executeQuery(\"select 1 from dual\")\n" +
                "  \tif(result.next()){\n" +
                "        logger.info(\"Запрос: ${result.getInt(1)}\")\n" +
                "  \t}\n" +
                "\n" +
                "    connection.close()\n" +
                "} catch (Exception ex) {\n" +
                "    logger.error(\"Ошибка: ${ex.getLocalizedMessage()}\")\n" +
                "}");
    }

    @Override
    public void execScript() {
        ScriptExecutionAction action = new ScriptExecutionAction();
        action.setScript(getView().getScriptCode());
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(new AbstractCallback<ScriptExecutionResult>() {
                    @Override
                    public void onSuccess(ScriptExecutionResult result) {
                        if (result.getUuid() != null) {
                            LogAddEvent.fire(ScriptExecutionPresenter.this, result.getUuid());
                        }
                    }
                }, this));
    }

    public interface MyView extends View, HasUiHandlers<ScriptExecutionUiHandlers> {
        String getScriptCode();

        void setScriptCode(String script);
    }


    @ProxyStandard
    @NameToken(ScriptExecutionTokens.SCRIPT_EXECUTION)
    public interface MyProxy extends ProxyPlace<ScriptExecutionPresenter> {
    }
}