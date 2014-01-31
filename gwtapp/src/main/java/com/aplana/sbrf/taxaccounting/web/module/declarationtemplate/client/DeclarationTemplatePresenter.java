package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DTCreateNewTypeEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client.event.CreateNewDTVersionEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.Date;

public class DeclarationTemplatePresenter extends Presenter<DeclarationTemplatePresenter.MyView, DeclarationTemplatePresenter.MyProxy>
		implements DeclarationTemplateUiHandlers, CreateNewDTVersionEvent.MyHandler, DTCreateNewTypeEvent.MyHandler {


    @Override
    @ProxyEvent
    public void onCreateVersion(CreateNewDTVersionEvent event) {
        GetDeclarationTypeAction action = new GetDeclarationTypeAction();
        action.setDeclarationTypeId(event.getTypeId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDeclarationTypeResult>() {
            @Override
            public void onSuccess(GetDeclarationTypeResult result) {
                declarationTemplateExt = new DeclarationTemplateExt();
                declarationTemplate = new DeclarationTemplate();
                declarationTemplateExt.setDeclarationTemplate(declarationTemplate);
                declarationTemplate.setVersion(new Date());
                declarationTemplate.setType(result.getDeclarationType());
                getView().setDeclarationTemplate(declarationTemplateExt);
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplate).
                        with(DeclarationTemplateTokens.declarationTemplateId, "0").build());
                TitleUpdateEvent.fire(DeclarationTemplatePresenter.this, "Шаблон декларации", declarationTemplate.getType().getName());

            }
        }, this).addCallback(new ManualRevealCallback<GetDeclarationTypeResult>(DeclarationTemplatePresenter.this)));
    }

    @Override
    @ProxyEvent
    public void onCreateDTType(final DTCreateNewTypeEvent event) {
        CreateNewDTTypeAction action = new CreateNewDTTypeAction();
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CreateNewDTTypeResult>() {
                    @Override
                    public void onSuccess(CreateNewDTTypeResult result) {
                        declarationTemplateExt = new DeclarationTemplateExt();
                        declarationTemplate = new DeclarationTemplate();
                        declarationTemplateExt.setDeclarationTemplate(declarationTemplate);
                        declarationTemplate.setVersion(new Date());
                        DeclarationType declarationType = new DeclarationType();
                        declarationType.setId(0);
                        declarationType.setName("");
                        declarationType.setStatus(VersionedObjectStatus.DRAFT);
                        declarationType.setTaxType(event.getTaxType());
                        declarationTemplate.setType(declarationType);
                        getView().setDeclarationTemplate(declarationTemplateExt);
                        TitleUpdateEvent.fire(DeclarationTemplatePresenter.this, "Шаблон декларации", declarationTemplate.getType().getName());
                    }
                }, this).addCallback(new ManualRevealCallback<CreateNewDTTypeResult>(DeclarationTemplatePresenter.this)));
    }

    @ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplate)
	public interface MyProxy extends ProxyPlace<DeclarationTemplatePresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<DeclarationTemplateUiHandlers> {
		void setDeclarationTemplate(DeclarationTemplateExt declaration);
        void addDeclarationValueHandler(ValueChangeHandler<String> valueChangeHandler);
        void activateButtonName(String name);
        void confirm();
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
    private DeclarationTemplateExt declarationTemplateExt;
	private DeclarationTemplate declarationTemplate;
	private HandlerRegistration closeDeclarationTemplateHandlerRegistration;

	@Inject
	public DeclarationTemplatePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	/**
	 * Здесь происходит подготовка декларации администрирования.
	 *
	 * @param request запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
        setDeclarationTemplate();
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void reset() {
		setDeclarationTemplate();
	}

	@Override
	public void onHide() {
		super.onHide();
        unlockForm(declarationTemplate.getId());
		closeDeclarationTemplateHandlerRegistration.removeHandler();
	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 */
	@Override
	public void save() {
        if (declarationTemplateExt.getEndDate() != null &&
                declarationTemplate.getVersion().compareTo(declarationTemplateExt.getEndDate()) >=0 ){
            MessageEvent.fire(DeclarationTemplatePresenter.this, "Дата окончания не может быть меньше даты начала актуализации.");
            return;
        }

        UpdateDeclarationAction action = new UpdateDeclarationAction();
		action.setDeclarationTemplateExt(declarationTemplateExt);
        dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<UpdateDeclarationResult>() {
					@Override
					public void onSuccess(UpdateDeclarationResult result) {
                        if (result.getLogUuid() != null)
                            LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getLogUuid());
						MessageEvent.fire(DeclarationTemplatePresenter.this, "Декларация сохранена");
                        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplate).
                                with(DeclarationTemplateTokens.declarationTemplateId, String.valueOf(result.getDeclarationTemplateId())).build());
						setDeclarationTemplate();
					}
				}, this).addCallback(new ManualRevealCallback<GetDeclarationResult>(DeclarationTemplatePresenter.this)));
	}

	/**
	 * Закрыть декларацию редактирования и вернуться на форму администрирования со списком шаблонов деклараций.
	 */
	@Override
	public void close() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationVersionList).
                with(DeclarationTemplateTokens.declarationType, String.valueOf(declarationTemplate.getType().getId())).build());
	}

    @Override
    public void activate() {
        if (declarationTemplate.getId() == null)
            return;
        SetActiveAction action = new SetActiveAction();
        action.setDtId(declarationTemplate.getId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SetActiveResult>() {
            @Override
            public void onSuccess(SetActiveResult result) {
                if (result.getUuid() != null)
                    LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getUuid());
                setDeclarationTemplate();
            }
        }, this));
    }

    @Override
	public void downloadJrxml() {
        if (declarationTemplate.getId() == null){
            getView().confirm();
            return;
        }
		Window.open(GWT.getHostPageBaseURL() + "download/downloadJrxml/" + declarationTemplate.getId(), null, null);
	}
	
	@Override
	public void downloadDect() {
        if (declarationTemplate.getId() == null){
            getView().confirm();
            return;
        }
		Window.open(GWT.getHostPageBaseURL() + "download/declarationTemplate/downloadDect/" + declarationTemplate.getId(), null, null);		
	}

	@Override
	public void uploadJrxmlFail(String error) {
		MessageEvent.fire(this, "Не удалось загрузить файл. Ошибка: " + error);
	}

	private void setDeclarationTemplate() {
        final int declarationId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationTemplateTokens.declarationTemplateId, "0"));
		if (declarationId != 0) {
			closeDeclarationTemplateHandlerRegistration = Window.addWindowClosingHandler(new Window.ClosingHandler() {
				@Override
				public void onWindowClosing(Window.ClosingEvent event) {
					unlockForm(declarationId);
					closeDeclarationTemplateHandlerRegistration.removeHandler();
				}
			});


			GetDeclarationAction action = new GetDeclarationAction();
			action.setId(declarationId);
			dispatcher.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<GetDeclarationResult>() {
						@Override
						public void onSuccess(GetDeclarationResult result) {
                            declarationTemplateExt = new DeclarationTemplateExt();
							declarationTemplate = result.getDeclarationTemplate();
                            getView().activateButtonName(declarationTemplate.getStatus().getId() == 0? "Вывести из действия" : "Ввести в действие");
                            declarationTemplateExt.setDeclarationTemplate(declarationTemplate);
                            declarationTemplateExt.setEndDate(result.getEndDate());
							getView().setDeclarationTemplate(declarationTemplateExt);
							TitleUpdateEvent.fire(DeclarationTemplatePresenter.this, "Шаблон декларации", declarationTemplate.getType().getName());
						}
					}, this).addCallback(new ManualRevealCallback<GetDeclarationResult>(DeclarationTemplatePresenter.this)));
        }
	}

	private void unlockForm(int declarationId){
		UnlockDeclarationAction action = new UnlockDeclarationAction();
		action.setDeclarationId(declarationId);
		dispatcher.execute(action, CallbackUtils.emptyCallback());
	}

	@Override
	public void uploadDectSuccess() {
		MessageEvent.fire(DeclarationTemplatePresenter.this, "Декларация импортирована");
		setDeclarationTemplate();
	}

	@Override
	public void uploadDectFail(String msg) {
		MessageEvent.fire(this, "Не удалось импортировать шаблон. Ошибка: " + msg);
	}

    @Override
    protected void onBind() {
        super.onBind();
        ValueChangeHandler<String> declarationValueChangeHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                declarationTemplate.setXsdId(event.getValue());
            }
        };
        getView().addDeclarationValueHandler(declarationValueChangeHandler);
    }
}
