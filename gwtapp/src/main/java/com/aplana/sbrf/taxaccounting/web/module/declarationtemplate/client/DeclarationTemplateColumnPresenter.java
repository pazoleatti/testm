package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTemplateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class DeclarationTemplateColumnPresenter
		extends Presenter<DeclarationTemplateColumnPresenter.MyView, DeclarationTemplateColumnPresenter.MyProxy>
		implements DeclarationTemplateColumnUiHandlers, UpdateTemplateEvent.MyHandler, DeclarationTemplateFlushEvent.MyHandler {


    @Title("Шаблоны")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateSubreports)
	@TabInfo(container = DeclarationTemplateMainPresenter.class, label = DeclarationTemplateTokens.declarationTemplateSubreportsLabel, priority = DeclarationTemplateTokens.declarationTemplateSubreportsPriority)
	public interface MyProxy extends
			TabContentProxyPlace<DeclarationTemplateColumnPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<DeclarationTemplateColumnUiHandlers> {
        void setDeclarationTemplate(DeclarationTemplateExt declaration);
        void flush();
	}

    private DeclarationTemplateMainPresenter declarationTemplateMainPresenter;

	@Inject
	public DeclarationTemplateColumnPresenter(final EventBus eventBus,
                                              final MyView view, final MyProxy proxy,
                                              DeclarationTemplateMainPresenter declarationTemplateMainPresenter) {
		super(eventBus, view, proxy, DeclarationTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
        this.declarationTemplateMainPresenter = declarationTemplateMainPresenter;
	}
	
	@Override
	protected void onBind() {
		super.onBind();
        addRegisteredHandler(UpdateTemplateEvent.getType(), this);
        addRegisteredHandler(DeclarationTemplateFlushEvent.getType(), this);
    }

    @ProxyEvent
    @Override
    public void onUpdateTemplate(UpdateTemplateEvent event) {
        getView().setDeclarationTemplate(declarationTemplateMainPresenter.getDeclarationTemplateExt());
    }

    @Override
    public void addSubreport(DeclarationSubreport subreport) {
        subreport.setDeclarationTemplateId(declarationTemplateMainPresenter.getDeclarationId());
        declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().addSubreport(subreport);
    }

    @Override
    public void removeSubreport(DeclarationSubreport subreport) {
        declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().removeSubreport(subreport);
    }

    @Override
    public void flushSubreport(DeclarationSubreport subreport) {
    }

    @Override
    public void onFlush(DeclarationTemplateFlushEvent event) {
        if (declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate() != null) {
            getView().flush();
        }
    }
}
