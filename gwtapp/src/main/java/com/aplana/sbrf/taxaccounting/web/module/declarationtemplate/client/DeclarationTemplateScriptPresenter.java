package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTemplateEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class DeclarationTemplateScriptPresenter
		extends Presenter<DeclarationTemplateScriptPresenter.MyView, DeclarationTemplateScriptPresenter.MyProxy>
		implements DeclarationTemplateScriptUiHandlers, UpdateTemplateEvent.MyHandler, DeclarationTemplateFlushEvent.MyHandler {


    @Title("Шаблоны")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateScript)
	@TabInfo(container = DeclarationTemplateMainPresenter.class, label = DeclarationTemplateTokens.declarationTemplateScriptLabel, priority = DeclarationTemplateTokens.declarationTemplateScriptPriority)
	public interface MyProxy extends
			TabContentProxyPlace<DeclarationTemplateScriptPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<DeclarationTemplateScriptUiHandlers> {
        void setScriptCode(String script);
        String getScriptCode();
//        void changeTableTopPosition(Boolean isLockInfoVisible);
	}

    private DeclarationTemplateMainPresenter declarationTemplateMainPresenter;

	@Inject
	public DeclarationTemplateScriptPresenter(final EventBus eventBus,
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
        getView().setScriptCode(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getCreateScript());
    }

    @Override
    public void onFlush(DeclarationTemplateFlushEvent event) {
        if (declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate() != null) {
            declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().setCreateScript(getView().getScriptCode());
        }
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this,
                DeclarationTemplateMainPresenter.TYPE_SetTabContent, this);

        // Workaround
        // Почему то тупит CodeMirror когда инициализация представления происходит до reveal
        getView().setScriptCode(getView().getScriptCode());
    }

    @Override
    public void onInfoChanged(){
        declarationTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
    }
}
