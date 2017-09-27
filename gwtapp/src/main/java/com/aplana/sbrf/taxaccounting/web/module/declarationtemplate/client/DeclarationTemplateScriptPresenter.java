package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateEventScript;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.AddScriptEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTemplateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event_script.EventScriptPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.AddScriptAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.AddScriptResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

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

        void setEventScriptList(List<DeclarationTemplateEventScript> eventList);

        void addEventScript(DeclarationTemplateEventScript eventScript);

        void removeEventScript(int index);

        void removeButtonEnable(boolean enable);

        String getSelectedTitle();
//        void changeTableTopPosition(Boolean isLockInfoVisible);
    }

    private DeclarationTemplateMainPresenter declarationTemplateMainPresenter;

    private EventScriptPresenter eventScriptPresenter;

    private final DispatchAsync dispatcher;

    @Inject
    public DeclarationTemplateScriptPresenter(final EventBus eventBus,
                                              final MyView view, final MyProxy proxy, final DispatchAsync dispatcher,
                                              final DeclarationTemplateMainPresenter declarationTemplateMainPresenter,
                                              EventScriptPresenter eventScriptPresenter) {
        super(eventBus, view, proxy, DeclarationTemplateMainPresenter.TYPE_SetTabContent);
        getView().setUiHandlers(this);
        this.declarationTemplateMainPresenter = declarationTemplateMainPresenter;
        this.eventScriptPresenter = eventScriptPresenter;
        eventBus.addHandler(AddScriptEvent.getType(), new AddScriptEvent.AddScriptHandler() {
            @Override
            public void onAdd(AddScriptEvent event) {
                AddScriptAction action = new AddScriptAction();
                action.setDeclarationTemplateId(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getId());
                action.setFormDataEventId(event.getFormDataEventId());
                dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<AddScriptResult>() {
                    @Override
                    public void onSuccess(AddScriptResult result) {
                        if (result.getDeclarationTemplateEventScript() != null) {
                            declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getEventScripts().add(result.getDeclarationTemplateEventScript());
                            getView().setEventScriptList(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getEventScripts());
                        } else {
                            Dialog.warningMessage("Скрипт для выбранного события существует.");
                        }
                    }
                }, DeclarationTemplateScriptPresenter.this));
            }
        });
        this.dispatcher = dispatcher;
        getView().removeButtonEnable(false);
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
        getView().setEventScriptList(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getEventScripts());
    }

    @Override
    public void onFlush(DeclarationTemplateFlushEvent event) {
        if (getView().getSelectedTitle().equals("Основной скрипт")) {
            declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().setCreateScript(getView().getScriptCode());
        } else {
            for (DeclarationTemplateEventScript declarationTemplateEventScript : declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getEventScripts()) {
                if (FormDataEvent.getByCode(declarationTemplateEventScript.getEventId()).getTitle().equals(getView().getSelectedTitle())) {
                    declarationTemplateEventScript.setScript(getView().getScriptCode());
                    break;
                }
            }
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
    public void onInfoChanged() {
        declarationTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
    }

    @Override
    public void onSelectScript(String title) {
        if (title.equals("Основной скрипт")) {
            getView().removeButtonEnable(false);
            getView().setScriptCode(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getCreateScript());
        } else {
            getView().removeButtonEnable(true);
            for (DeclarationTemplateEventScript declarationTemplateEventScript : declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getEventScripts()) {
                if (FormDataEvent.getByCode(declarationTemplateEventScript.getEventId()).getTitle().equals(title)) {
                    getView().setScriptCode(declarationTemplateEventScript.getScript());
                    break;
                }
            }
        }
    }

    @Override
    public void onOpenEventChoiceDialog() {
        addToPopupSlot(eventScriptPresenter);
    }

    @Override
    public void onRemoveEventScript(final int index) {
        DeclarationTemplateEventScript eventScriptForRemove = null;
        for (DeclarationTemplateEventScript declarationTemplateEventScript : declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getEventScripts()) {
            if (FormDataEvent.getByCode(declarationTemplateEventScript.getEventId()).getTitle().equals(getView().getSelectedTitle())) {
                eventScriptForRemove = declarationTemplateEventScript;
                break;
            }
        }
        declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getEventScripts().remove(eventScriptForRemove);
        getView().setEventScriptList(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getEventScripts());
        /*getView().removeEventScript(index);*/
        getView().removeButtonEnable(false);
        onSelectScript("Основной скрипт");
    }
}
