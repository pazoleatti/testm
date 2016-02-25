package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.web.main.api.client.DownloadUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DTCreateNewTypeEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTemplateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client.event.CreateNewDTVersionEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.JrxmlFileExistEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client.DeclarationVersionHistoryPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.Date;

public class DeclarationTemplateMainPresenter extends TabContainerPresenter<DeclarationTemplateMainPresenter.MyView, DeclarationTemplateMainPresenter.MyProxy>
		implements DeclarationTemplateMainUiHandlers, CreateNewDTVersionEvent.MyHandler, DTCreateNewTypeEvent.MyHandler {

    private static final String ERROR_MSG = "Не удалось загрузить макет";
    private static final String SUCCESS_MSG = "Файл загружен";
    private static final String JRXML_INFO_MES =
            "Загрузка нового jrxml файла приведет к удалению уже сформированных pdf, xlsx отчетов и отмене ранее запущенных операций формирования pdf, xlsx отчетов экземпляров деклараций данной версии макета. Продолжить?";

    @RequestTabs
    public static final GwtEvent.Type<RequestTabsHandler> TYPE_RequestTabs = new GwtEvent.Type<RequestTabsHandler>();

    @ChangeTab
    public static final GwtEvent.Type<ChangeTabHandler> TYPE_ChangeTab = new GwtEvent.Type<ChangeTabHandler>();

    @ContentSlot
    public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SetTabContent = new GwtEvent.Type<RevealContentHandler<?>>();

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
                getView().setTemplateId(0);
                getView().setDeclarationTemplate(declarationTemplateExt);
                UpdateTemplateEvent.fire(DeclarationTemplateMainPresenter.this);
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplateInfo).
                        with(DeclarationTemplateTokens.declarationTemplateId, "0").build());
                TitleUpdateEvent.fire(DeclarationTemplateMainPresenter.this, "Шаблон декларации", declarationTemplate.getType().getName());

            }
        }, this).addCallback(new ManualRevealCallback<GetDeclarationTypeResult>(DeclarationTemplateMainPresenter.this)));
    }

    @Override
    @ProxyEvent
    public void onCreateDTType(final DTCreateNewTypeEvent event) {
        declarationTemplateExt = new DeclarationTemplateExt();
        declarationTemplate = new DeclarationTemplate();
        declarationTemplateExt.setDeclarationTemplate(declarationTemplate);
        declarationTemplateExt.setEndDate(null);
        declarationTemplate.setVersion(new Date());
        DeclarationType declarationType = new DeclarationType();
        declarationType.setId(0);
        declarationType.setName("");
        declarationType.setStatus(VersionedObjectStatus.DRAFT);
        declarationType.setTaxType(event.getTaxType());
        declarationTemplate.setType(declarationType);
        getView().setTemplateId(0);
        getView().setDeclarationTemplate(declarationTemplateExt);
        UpdateTemplateEvent.fire(DeclarationTemplateMainPresenter.this);
        TitleUpdateEvent.fire(DeclarationTemplateMainPresenter.this, "Шаблон декларации", declarationTemplate.getType().getName());
        RevealContentEvent.fire(DeclarationTemplateMainPresenter.this, RevealContentTypeHolder.getMainContent(), DeclarationTemplateMainPresenter.this);
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplateInfo).
                with(DeclarationTemplateTokens.declarationTemplateId, "0").build());

    }

    @ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplate)
	public interface MyProxy extends ProxyPlace<DeclarationTemplateMainPresenter>, Place {
	}

	public interface MyView extends TabView, HasUiHandlers<DeclarationTemplateMainUiHandlers> {
        void setDeclarationTemplate(DeclarationTemplateExt declaration);
        HandlerRegistration addChangeHandlerDect(ValueChangeHandler<String> valueChangeHandler);
        HandlerRegistration addStartLoadHandlerDect(StartLoadFileEvent.StartLoadFileHandler handler);
        HandlerRegistration addEndLoadHandlerDect(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addJrxmlLoadHandlerDect(JrxmlFileExistEvent.JrxmlFileExistHandler handler);
        void activateButtonName(String name);
        void activateButton(boolean isVisible);
        void setLockInformation(boolean isVisible, String lockDate, String lockedBy);
        void setTemplateId(int templateId);
    }

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
    private DeclarationTemplateExt declarationTemplateExt;
	private DeclarationTemplate declarationTemplate;
	private HandlerRegistration closeDeclarationTemplateHandlerRegistration;
    protected DeclarationVersionHistoryPresenter versionHistoryPresenter;

	@Inject
    public DeclarationTemplateMainPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager,
                                            DeclarationVersionHistoryPresenter versionHistoryPresenter) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
        this.versionHistoryPresenter = versionHistoryPresenter;
	}

    @Override
    public void downloadDect() {
        if (declarationTemplate.getId() != null) {
            DownloadUtils.openInIframe(
                    GWT.getHostPageBaseURL() + "download/declarationTemplate/downloadDect/" + declarationTemplate.getId());
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setDeclarationTemplate();
        LogCleanEvent.fire(DeclarationTemplateMainPresenter.this);
    }

	@Override
	public void reset() {
		setDeclarationTemplate();
	}

	@Override
	public void onHide() {
		super.onHide();
        unlockForm(declarationTemplate.getId() != null ? declarationTemplate.getId() : 0);
		if (closeDeclarationTemplateHandlerRegistration != null)
            closeDeclarationTemplateHandlerRegistration.removeHandler();
	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 */
	@Override
	public void save() {
        DeclarationTemplateFlushEvent.fire(DeclarationTemplateMainPresenter.this);
        if (declarationTemplateExt.getEndDate() != null &&
                declarationTemplate.getVersion().compareTo(declarationTemplateExt.getEndDate()) >0 ){
            Dialog.infoMessage("Дата окончания не может быть меньше даты начала актуализации.");
            return;
        }
        if (declarationTemplate.getName() == null || declarationTemplate.getName().isEmpty()){
            Dialog.infoMessage("Введите имя декларации");
            return;
        }

        if (declarationTemplate.getId() == null && declarationTemplate.getType().getId() == 0){
            CreateNewDeclarationTypeAction action = new CreateNewDeclarationTypeAction();
            action.setDeclarationTemplateExt(declarationTemplateExt);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CreateNewDeclarationTypeResult>() {
                @Override
                public void onSuccess(CreateNewDeclarationTypeResult result) {
                    LogCleanEvent.fire(DeclarationTemplateMainPresenter.this);
                    LogAddEvent.fire(DeclarationTemplateMainPresenter.this, result.getLogUuid());
                    Dialog.infoMessage("Декларация сохранена");
                    declarationTemplate.setId(result.getDeclarationTemplateId());
                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplate).
                            with(DeclarationTemplateTokens.declarationTemplateId, String.valueOf(result.getDeclarationTemplateId())).build());
                    reset();
                    getView().activateButton(true);
                }
            }, this));
        } else if (declarationTemplate.getId() == null){
            CreateNewDTVersionAction action = new CreateNewDTVersionAction();
            action.setDeclarationTemplateExt(declarationTemplateExt);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CreateNewDTVersionResult>() {
                @Override
                public void onSuccess(CreateNewDTVersionResult result) {
                    LogCleanEvent.fire(DeclarationTemplateMainPresenter.this);
                    LogAddEvent.fire(DeclarationTemplateMainPresenter.this, result.getLogUuid());
                    Dialog.infoMessage("Декларация сохранена");
                    declarationTemplate.setId(result.getDeclarationTemplateId());
                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplate).
                            with(DeclarationTemplateTokens.declarationTemplateId, String.valueOf(result.getDeclarationTemplateId())).build());
                    getView().setDeclarationTemplate(declarationTemplateExt);
                    reset();
                    getView().activateButton(true);
                }
            }, this));
        } else {
            UpdateDeclarationAction action = new UpdateDeclarationAction();
            action.setDeclarationTemplateExt(declarationTemplateExt);
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<UpdateDeclarationResult>() {
                        @Override
                        public void onSuccess(UpdateDeclarationResult result) {
                            LogCleanEvent.fire(DeclarationTemplateMainPresenter.this);
                            LogAddEvent.fire(DeclarationTemplateMainPresenter.this, result.getLogUuid());
                            Dialog.infoMessage("Декларация сохранена");
                        }
                    }, this));
        }

	}

	/**
	 * Закрыть декларацию редактирования и вернуться на форму администрирования со списком версий шаблонов деклараций.
	 */
	@Override
	public void close() {
        if (declarationTemplate.getType().getId() == 0)
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplateList).build());
        else
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationVersionList).
                with(DeclarationTemplateTokens.declarationType, String.valueOf(declarationTemplate.getType().getId())).build());
	}

    @Override
    public void activate(boolean force) {
        if (declarationTemplate.getId() == null)
            return;
        SetActiveAction action = new SetActiveAction();
        action.setDtId(declarationTemplate.getId());
        action.setForce(force);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SetActiveResult>() {
            @Override
            public void onSuccess(SetActiveResult result) {
                LogAddEvent.fire(DeclarationTemplateMainPresenter.this, result.getUuid());
                if (!result.isSetStatusSuccessfully()) { //
                    Dialog.confirmMessage("Информация",
                            "Найдены экземпляры деклараций, использующие версию макета. Изменить статус версии?",
                            new DialogHandler() {
                                @Override
                                public void yes() {
                                    activate(true);
                                    super.yes();
                                }
                            });
                } else {
                    declarationTemplate.setStatus(VersionedObjectStatus.getStatusById(result.getStatus()));
                    getView().activateButtonName(result.getStatus() == 0 ? "Вывести из действия" : "Ввести в действие");
                }
            }
        }, this));
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
                            LogAddEvent.fire(DeclarationTemplateMainPresenter.this, result.getUuid());
                            if (result.isLockedByAnotherUser()) {
                                getView().setLockInformation(true, result.getLockDate(), result.getLockedByUser());
                            } else {
                                getView().setLockInformation(false, null, null);
                            }
                            declarationTemplateExt = new DeclarationTemplateExt();
							declarationTemplate = result.getDeclarationTemplate();
                            getView().setTemplateId(declarationTemplate.getId());
                            getView().activateButtonName(declarationTemplate.getStatus().getId() == 0? "Вывести из действия" : "Ввести в действие");
                            getView().activateButton(true);
                            declarationTemplateExt.setDeclarationTemplate(declarationTemplate);
                            declarationTemplateExt.setEndDate(result.getEndDate());
							getView().setDeclarationTemplate(declarationTemplateExt);
							TitleUpdateEvent.fire(DeclarationTemplateMainPresenter.this, "Шаблон декларации", declarationTemplate.getType().getName());
                            UpdateTemplateEvent.fire(DeclarationTemplateMainPresenter.this);
                            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplateInfo).
                                    with(DeclarationTemplateTokens.declarationTemplateId, String.valueOf(declarationId)).build());
						}
					}, this).addCallback(new ManualRevealCallback<GetDeclarationResult>(DeclarationTemplateMainPresenter.this)));
        } else {
            getView().activateButton(false);
            getView().setLockInformation(false, null, null);
        }
	}

	private void unlockForm(int declarationId){
        if (declarationId == 0)
            return;
        UnlockDeclarationAction action = new UnlockDeclarationAction();
		action.setDeclarationId(declarationId);
		dispatcher.execute(action, CallbackUtils.emptyCallback());
	}

    @Override
    public int getDeclarationId() {
        return declarationTemplate.getId() != null ? declarationTemplate.getId() : 0;
    }

    @Override
    public void onHistoryClicked() {
        Integer id = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(DeclarationTemplateTokens.declarationTemplateId, ""));
        if (id == 0)
            return;
        versionHistoryPresenter.init(id);
        addToPopupSlot(versionHistoryPresenter);
    }

    private HandlerRegistration[] handlerRegistrations = new HandlerRegistration[4];

    @Override
    protected void onBind() {
        super.onBind();
        addRegisteredHandler(CreateNewDTVersionEvent.getType(), this);
        addRegisteredHandler(DTCreateNewTypeEvent.getType(), this);

        ValueChangeHandler<String> vchDect = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Dialog.infoMessage(SUCCESS_MSG);
            }
        };
        StartLoadFileEvent.StartLoadFileHandler startLoadFileHandler = new StartLoadFileEvent.StartLoadFileHandler() {
            @Override
            public void onStartLoad(StartLoadFileEvent event) {
                // Чистим логи и блокируем форму
                LogCleanEvent.fire(DeclarationTemplateMainPresenter.this);
                LockInteractionEvent.fire(DeclarationTemplateMainPresenter.this, true);
            }
        };
        EndLoadFileEvent.EndLoadFileHandler loadFileHandlerDect = new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                LockInteractionEvent.fire(DeclarationTemplateMainPresenter.this, false);
                if (event.isHasError()){
                    Dialog.errorMessage(ERROR_MSG);
                } else if (event.getUuid() == null || event.getUuid().isEmpty()){// TODO проверить условие
                    reset();
                    Dialog.infoMessage("Макет успешно обновлен");
                }
                LogAddEvent.fire(DeclarationTemplateMainPresenter.this, event.getUuid());
            }
        };

        handlerRegistrations[0] = getView().addChangeHandlerDect(vchDect);
        handlerRegistrations[1] = getView().addEndLoadHandlerDect(loadFileHandlerDect);
        handlerRegistrations[2] = getView().addJrxmlLoadHandlerDect(getJrxmlFileExistHandler(true));
        handlerRegistrations[3] = getView().addStartLoadHandlerDect(startLoadFileHandler);
    }

    public JrxmlFileExistEvent.JrxmlFileExistHandler getJrxmlFileExistHandler(final boolean isArchive){
        return new JrxmlFileExistEvent.JrxmlFileExistHandler() {
            @Override
            public void onJrxmlExist(final JrxmlFileExistEvent event) {
                LogAddEvent.fire(DeclarationTemplateMainPresenter.this, event.getErrorUuid());
                Dialog.confirmMessage("Загрузка jrxml файла", JRXML_INFO_MES,
                        new DialogHandler() {
                            @Override
                            public void yes() {
                                super.yes();
                                DeleteJrxmlAction deleteJrxmlAction = new DeleteJrxmlAction();
                                deleteJrxmlAction.setDtId(getDeclarationId());
                                dispatcher.execute(deleteJrxmlAction, CallbackUtils.defaultCallback(new AbstractCallback<DeleteJrxmlResult>() {
                                    @Override
                                    public void onSuccess(DeleteJrxmlResult result) {
                                        ResidualSaveAction action = new ResidualSaveAction();
                                        action.setDtId(getDeclarationId());
                                        action.setUploadUuid(event.getUploadUuid());
                                        action.setIsArchive(isArchive);
                                        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<ResidualSaveResult>() {
                                            @Override
                                            public void onSuccess(ResidualSaveResult result) {
                                                declarationTemplateExt.getDeclarationTemplate().setJrxmlBlobId(event.getErrorUuid());
                                                Dialog.infoMessage("Макет успешно обновлен");
                                                LogCleanEvent.fire(DeclarationTemplateMainPresenter.this);
                                                LogAddEvent.fire(DeclarationTemplateMainPresenter.this, result.getSuccessUuid(), true);
                                            }
                                        }, DeclarationTemplateMainPresenter.this));
                                    }
                                }, DeclarationTemplateMainPresenter.this));
                            }
                        });
            }
        };
    }

    @Override
    protected void onUnbind() {
        super.onUnbind();
        for (HandlerRegistration han : handlerRegistrations){
            han.removeHandler();
        }
    }

    public DeclarationTemplateExt getDeclarationTemplateExt() {
        return declarationTemplateExt;
    }
}
