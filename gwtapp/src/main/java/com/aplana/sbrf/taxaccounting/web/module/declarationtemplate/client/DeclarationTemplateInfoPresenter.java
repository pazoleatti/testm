package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.main.api.client.DownloadUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTemplateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.JrxmlFileExistEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.Date;

public class DeclarationTemplateInfoPresenter
		extends Presenter<DeclarationTemplateInfoPresenter.MyView, DeclarationTemplateInfoPresenter.MyProxy>
		implements DeclarationTemplateInfoUiHandlers, UpdateTemplateEvent.MyHandler, DeclarationTemplateFlushEvent.MyHandler {

    private static final String ERROR_MSG = "Не удалось загрузить макет";
    private static final String SUCCESS_MSG = "Файл загружен";

    private HandlerRegistration[] handlerRegistrations = new HandlerRegistration[7];

    @Override
    public void onCheckBeforeDeleteJrxml() {
        final CheckJrxmlAction action = new CheckJrxmlAction();
        action.setDtId(declarationTemplateMainPresenter.getDeclarationId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CheckJrxmlResult>() {
            @Override
            public void onSuccess(final CheckJrxmlResult result) {
                if (result.isCanDelete()){
                    DeleteJrxmlAction jrxmlAction = new DeleteJrxmlAction();
                    jrxmlAction.setDtId(declarationTemplateMainPresenter.getDeclarationId());
                    dispatcher.execute(jrxmlAction, CallbackUtils.defaultCallback(new AbstractCallback<DeleteJrxmlResult>() {
                        @Override
                        public void onSuccess(DeleteJrxmlResult result) {
                            LogCleanEvent.fire(DeclarationTemplateInfoPresenter.this);
                            declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().setJrxmlBlobId(null);
                            getView().setDeclarationTemplate(declarationTemplateMainPresenter.getDeclarationTemplateExt());
                        }
                    }, DeclarationTemplateInfoPresenter.this));
                } else {
                    LogCleanEvent.fire(DeclarationTemplateInfoPresenter.this);
                    LogAddEvent.fire(DeclarationTemplateInfoPresenter.this, result.getUuid());
                    Dialog.confirmMessage("Удаление jrxml файла",
                            "Удаление jrxml файла приведет к удалению уже сформированных pdf, xlsx отчетов и отмене ранее запущенных операций формирования pdf, xlsx отчетов экземпляров деклараций данной версии макета. Продолжить?",
                            new DialogHandler() {
                                @Override
                                public void yes() {
                                    DeleteJrxmlAction jrxmlAction = new DeleteJrxmlAction();
                                    jrxmlAction.setDtId(declarationTemplateMainPresenter.getDeclarationId());
                                    dispatcher.execute(jrxmlAction, CallbackUtils.defaultCallback(new AbstractCallback<DeleteJrxmlResult>() {
                                        @Override
                                        public void onSuccess(DeleteJrxmlResult result) {
                                            LogCleanEvent.fire(DeclarationTemplateInfoPresenter.this);
                                            declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().setJrxmlBlobId(null);
                                            getView().setDeclarationTemplate(declarationTemplateMainPresenter.getDeclarationTemplateExt());
                                        }
                                    }, DeclarationTemplateInfoPresenter.this));
                                }
                            });
                }
            }
        }, this));
    }

    @Override
    public void downloadJrxml() {
        if (declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getJrxmlBlobId() != null && !declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getJrxmlBlobId().isEmpty()) {
            DownloadUtils.openInIframe(GWT.getHostPageBaseURL() + "download/downloadByUuid/" + declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getJrxmlBlobId());
        }
    }

    @Override
    public void downloadXsd() {
        if (declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getXsdId() != null && !declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getXsdId().isEmpty()) {
            DownloadUtils.openInIframe(GWT.getHostPageBaseURL() + "download/downloadByUuid/" + declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getXsdId());
        }
    }

    @Override
    public void onDeleteXsd() {
        DeleteXsdAction action = new DeleteXsdAction();
        action.setDtId(declarationTemplateMainPresenter.getDeclarationId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DeleteXsdResult>() {
            @Override
            public void onSuccess(DeleteXsdResult result) {
                declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().setXsdId(null);
                getView().setDeclarationTemplate(declarationTemplateMainPresenter.getDeclarationTemplateExt());
            }
        }, this));
    }

    @Title("Шаблоны")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateInfo)
	@TabInfo(container = DeclarationTemplateMainPresenter.class, label = DeclarationTemplateTokens.declarationTemplateInfoLabel, priority = DeclarationTemplateTokens.declarationTemplateInfoPriority)
	public interface MyProxy extends
			TabContentProxyPlace<DeclarationTemplateInfoPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<DeclarationTemplateInfoUiHandlers> {
        void setDeclarationTemplate(DeclarationTemplateExt declaration);
        HandlerRegistration addValueChangeHandlerJrxml(ValueChangeHandler<String> valueChangeHandler);
        HandlerRegistration addValueChangeHandlerXsd(ValueChangeHandler<String> valueChangeHandler);
        HandlerRegistration addStartLoadHandlerXsd(StartLoadFileEvent.StartLoadFileHandler handler);
        HandlerRegistration addStartLoadHandlerJrxml(StartLoadFileEvent.StartLoadFileHandler handler);
        HandlerRegistration addEndLoadHandlerXsd(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addEndLoadHandlerJrxml(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addJrxmlLoadHandler(JrxmlFileExistEvent.JrxmlFileExistHandler handler);
        Date getStartDate();
        Date getEndDate();
        String getName();
	}

    private final DispatchAsync dispatcher;
    private DeclarationTemplateMainPresenter declarationTemplateMainPresenter;

	@Inject
	public DeclarationTemplateInfoPresenter(final EventBus eventBus,
                                            final MyView view, final MyProxy proxy, DispatchAsync dispatcher,
                                            DeclarationTemplateMainPresenter declarationTemplateMainPresenter,
                                            PlaceManager placeManager) {
		super(eventBus, view, proxy, DeclarationTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
        this.dispatcher = dispatcher;
        this.declarationTemplateMainPresenter = declarationTemplateMainPresenter;
	}
	
	@Override
	protected void onBind() {
		super.onBind();
        addRegisteredHandler(UpdateTemplateEvent.getType(), this);
        addRegisteredHandler(DeclarationTemplateFlushEvent.getType(), this);

        ValueChangeHandler<String> vchJrxml = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Dialog.infoMessage(SUCCESS_MSG);
                declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().setJrxmlBlobId(event.getValue());
                getView().setDeclarationTemplate(declarationTemplateMainPresenter.getDeclarationTemplateExt());
            }
        };
        ValueChangeHandler<String> vchXsd = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Dialog.infoMessage(SUCCESS_MSG);
                declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().setXsdId(event.getValue());
                getView().setDeclarationTemplate(declarationTemplateMainPresenter.getDeclarationTemplateExt());
            }
        };
        EndLoadFileEvent.EndLoadFileHandler loadFileHandlerContent = new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                if (event.isHasError()){
                    Dialog.errorMessage(ERROR_MSG);
                }
                LogAddEvent.fire(DeclarationTemplateInfoPresenter.this, event.getUuid());
                LockInteractionEvent.fire(DeclarationTemplateInfoPresenter.this, false);
            }
        };
        StartLoadFileEvent.StartLoadFileHandler startLoadFileHandler = new StartLoadFileEvent.StartLoadFileHandler() {
            @Override
            public void onStartLoad(StartLoadFileEvent event) {
                // Чистим логи и блокируем форму
                LogCleanEvent.fire(DeclarationTemplateInfoPresenter.this);
                LockInteractionEvent.fire(DeclarationTemplateInfoPresenter.this, true);
            }
        };

        handlerRegistrations[0] = getView().addValueChangeHandlerJrxml(vchJrxml);
        handlerRegistrations[1] = getView().addValueChangeHandlerXsd(vchXsd);
        handlerRegistrations[2] = getView().addEndLoadHandlerXsd(loadFileHandlerContent);
        handlerRegistrations[3] = getView().addEndLoadHandlerJrxml(loadFileHandlerContent);
        handlerRegistrations[4] = getView().addJrxmlLoadHandler(declarationTemplateMainPresenter.getJrxmlFileExistHandler(false, DeclarationTemplateInfoPresenter.this));
        handlerRegistrations[5] = getView().addStartLoadHandlerXsd(startLoadFileHandler);
        handlerRegistrations[6] = getView().addStartLoadHandlerJrxml(startLoadFileHandler);
    }

    @ProxyEvent
    @Override
    public void onUpdateTemplate(UpdateTemplateEvent event) {
        getView().setDeclarationTemplate(declarationTemplateMainPresenter.getDeclarationTemplateExt());
    }

    @Override
    public void onFlush(DeclarationTemplateFlushEvent event) {
        DeclarationTemplateExt templateExt = declarationTemplateMainPresenter.getDeclarationTemplateExt();
        templateExt.getDeclarationTemplate().setName(getView().getName());
        templateExt.getDeclarationTemplate().setVersion(getView().getStartDate());
        templateExt.setEndDate(getView().getEndDate());
    }

    @Override
    public void onInfoChanged(){
        declarationTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
    }

    @Override
    protected void onUnbind() {
        super.onUnbind();
        for (HandlerRegistration han : handlerRegistrations){
            han.removeHandler();
        }
    }
}
