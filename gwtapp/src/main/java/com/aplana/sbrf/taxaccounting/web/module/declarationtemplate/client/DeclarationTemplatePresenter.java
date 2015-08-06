package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DTCreateNewTypeEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client.event.CreateNewDTVersionEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.JrxmlFileExistEvent;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client.DeclarationVersionHistoryPresenter;
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

    private static final String ERROR_MSG = "Не удалось загрузить макет";
    private static final String SUCCESS_MSG = "Файл загружен";
    private static final String JRXML_INFO_MES =
            "Загрузка нового jrxml файла приведет к удалению уже сформированных pdf, xlsx отчетов и отмене ранее запущенных операций формирования pdf, xlsx отчетов экземпляров деклараций данной версии макета. Продолжить?";

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
        getView().setDeclarationTemplate(declarationTemplateExt);
        TitleUpdateEvent.fire(DeclarationTemplatePresenter.this, "Шаблон декларации", declarationTemplate.getType().getName());
        RevealContentEvent.fire(DeclarationTemplatePresenter.this, RevealContentTypeHolder.getMainContent(), DeclarationTemplatePresenter.this);
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplate).
                with(DeclarationTemplateTokens.declarationTemplateId, "0").build());

    }

    @ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplate)
	public interface MyProxy extends ProxyPlace<DeclarationTemplatePresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<DeclarationTemplateUiHandlers> {
        void setDeclarationTemplate(DeclarationTemplateExt declaration);
        HandlerRegistration addValueChangeHandlerJrxml(ValueChangeHandler<String> valueChangeHandler);
        HandlerRegistration addValueChangeHandlerXsd(ValueChangeHandler<String> valueChangeHandler);
        HandlerRegistration addChangeHandlerDect(ValueChangeHandler<String> valueChangeHandler);
        HandlerRegistration addEndLoadHandlerXsd(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addEndLoadHandlerJrxml(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addEndLoadHandlerDect(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addJrxmlLoadHandlerDect(JrxmlFileExistEvent.JrxmlFileExistHandler handler);
        HandlerRegistration addJrxmlLoadHandler(JrxmlFileExistEvent.JrxmlFileExistHandler handler);
        void activateButtonName(String name);
        void activateButton(boolean isVisible);
        void setLockInformation(boolean isVisible, String lockDate, String lockedBy);
	}

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
    private DeclarationTemplateExt declarationTemplateExt;
	private DeclarationTemplate declarationTemplate;
	private HandlerRegistration closeDeclarationTemplateHandlerRegistration;
    protected DeclarationVersionHistoryPresenter versionHistoryPresenter;

	@Inject
    public DeclarationTemplatePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager,
                                        DeclarationVersionHistoryPresenter versionHistoryPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
        this.versionHistoryPresenter = versionHistoryPresenter;
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
                    LogCleanEvent.fire(DeclarationTemplatePresenter.this);
                    LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getLogUuid());
                    Dialog.infoMessage("Декларация сохранена");
                    declarationTemplate.setId(result.getDeclarationTemplateId());
                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplate).
                            with(DeclarationTemplateTokens.declarationTemplateId, String.valueOf(result.getDeclarationTemplateId())).build());
                    getView().setDeclarationTemplate(declarationTemplateExt);
                    getView().activateButton(true);
                }
            }, this));
        } else if (declarationTemplate.getId() == null){
            CreateNewDTVersionAction action = new CreateNewDTVersionAction();
            action.setDeclarationTemplateExt(declarationTemplateExt);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CreateNewDTVersionResult>() {
                @Override
                public void onSuccess(CreateNewDTVersionResult result) {
                    LogCleanEvent.fire(DeclarationTemplatePresenter.this);
                    LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getLogUuid());
                    Dialog.infoMessage("Декларация сохранена");
                    declarationTemplate.setId(result.getDeclarationTemplateId());
                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(DeclarationTemplateTokens.declarationTemplate).
                            with(DeclarationTemplateTokens.declarationTemplateId, String.valueOf(result.getDeclarationTemplateId())).build());
                    getView().setDeclarationTemplate(declarationTemplateExt);
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
                            LogCleanEvent.fire(DeclarationTemplatePresenter.this);
                            LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getLogUuid());
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
                LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getUuid());
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
                            LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getUuid());
                            if (result.isLockedByAnotherUser()) {
                                getView().setLockInformation(true, result.getLockDate(), result.getLockedByUser());
                            } else {
                                getView().setLockInformation(false, null, null);
                            }
                            declarationTemplateExt = new DeclarationTemplateExt();
							declarationTemplate = result.getDeclarationTemplate();
                            getView().activateButtonName(declarationTemplate.getStatus().getId() == 0? "Вывести из действия" : "Ввести в действие");
                            getView().activateButton(true);
                            declarationTemplateExt.setDeclarationTemplate(declarationTemplate);
                            declarationTemplateExt.setEndDate(result.getEndDate());
							getView().setDeclarationTemplate(declarationTemplateExt);
							TitleUpdateEvent.fire(DeclarationTemplatePresenter.this, "Шаблон декларации", declarationTemplate.getType().getName());
						}
					}, this).addCallback(new ManualRevealCallback<GetDeclarationResult>(DeclarationTemplatePresenter.this)));
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

    @Override
    public void onDeleteXsd() {
        DeleteXsdAction action = new DeleteXsdAction();
        action.setDtId(declarationTemplate.getId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<DeleteXsdResult>() {
            @Override
            public void onSuccess(DeleteXsdResult result) {
                declarationTemplate.setXsdId(null);
                getView().setDeclarationTemplate(declarationTemplateExt);
            }
        }, this));
    }

    @Override
    public void onCheckBeforeDeleteJrxml() {
        final CheckJrxmlAction action = new CheckJrxmlAction();
        action.setDtId(declarationTemplate.getId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CheckJrxmlResult>() {
            @Override
            public void onSuccess(final CheckJrxmlResult result) {
                if (result.isCanDelete()){
                    DeleteJrxmlAction jrxmlAction = new DeleteJrxmlAction();
                    jrxmlAction.setDtId(declarationTemplate.getId());
                    dispatcher.execute(jrxmlAction, CallbackUtils.defaultCallback(new AbstractCallback<DeleteJrxmlResult>() {
                        @Override
                        public void onSuccess(DeleteJrxmlResult result) {
                            LogCleanEvent.fire(DeclarationTemplatePresenter.this);
                            declarationTemplate.setJrxmlBlobId(null);
                            getView().setDeclarationTemplate(declarationTemplateExt);
                        }
                    }, DeclarationTemplatePresenter.this));
                } else {
                    LogCleanEvent.fire(DeclarationTemplatePresenter.this);
                    LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getUuid());
                    Dialog.confirmMessage("Удаление jrxml файла",
                            "Удаление jrxml файла приведет к удалению уже сформированных pdf, xlsx отчетов и отмене ранее запущенных операций формирования pdf, xlsx отчетов экземпляров деклараций данной версии макета. Продолжить?",
                            new DialogHandler() {
                                @Override
                                public void yes() {
                                    DeleteJrxmlAction jrxmlAction = new DeleteJrxmlAction();
                                    jrxmlAction.setDtId(declarationTemplate.getId());
                                    dispatcher.execute(jrxmlAction, CallbackUtils.defaultCallback(new AbstractCallback<DeleteJrxmlResult>() {
                                        @Override
                                        public void onSuccess(DeleteJrxmlResult result) {
                                            LogCleanEvent.fire(DeclarationTemplatePresenter.this);
                                            declarationTemplate.setJrxmlBlobId(null);
                                            getView().setDeclarationTemplate(declarationTemplateExt);
                                        }
                                    }, DeclarationTemplatePresenter.this));
                                }
                            });
                }
            }
        }, this));
    }

    private HandlerRegistration[] handlerRegistrations = new HandlerRegistration[8];
    @Override
    protected void onBind() {
        super.onBind();

        ValueChangeHandler<String> vchJrxml = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Dialog.infoMessage(SUCCESS_MSG);
                declarationTemplateExt.getDeclarationTemplate().setJrxmlBlobId(event.getValue());
                getView().setDeclarationTemplate(declarationTemplateExt);
            }
        };
        ValueChangeHandler<String> vchXsd = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Dialog.infoMessage(SUCCESS_MSG);
                declarationTemplateExt.getDeclarationTemplate().setXsdId(event.getValue());
                getView().setDeclarationTemplate(declarationTemplateExt);
            }
        };
        ValueChangeHandler<String> vchDect = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Dialog.infoMessage(SUCCESS_MSG);
            }
        };
        EndLoadFileEvent.EndLoadFileHandler loadFileHandlerXsd = new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                if (event.isHasError()){
                    Dialog.errorMessage(ERROR_MSG);
                }
                LogAddEvent.fire(DeclarationTemplatePresenter.this, event.getUuid());
            }
        };
        EndLoadFileEvent.EndLoadFileHandler loadFileHandlerJrxml = new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                if (event.isHasError()){
                    Dialog.errorMessage(ERROR_MSG);
                }
                LogAddEvent.fire(DeclarationTemplatePresenter.this, event.getUuid());
            }
        };
        EndLoadFileEvent.EndLoadFileHandler loadFileHandlerDect = new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                if (event.isHasError()){
                    Dialog.errorMessage(ERROR_MSG);
                } else if (event.getUuid() == null || event.getUuid().isEmpty()){// TODO проверить условие
                    reset();
                    Dialog.infoMessage("Макет успешно обновлен");
                }
                LogAddEvent.fire(DeclarationTemplatePresenter.this, event.getUuid());
            }
        };

        handlerRegistrations[0] = getView().addValueChangeHandlerJrxml(vchJrxml);
        handlerRegistrations[1] = getView().addValueChangeHandlerXsd(vchXsd);
        handlerRegistrations[2] = getView().addChangeHandlerDect(vchDect);
        handlerRegistrations[3] = getView().addEndLoadHandlerXsd(loadFileHandlerXsd);
        handlerRegistrations[4] = getView().addEndLoadHandlerJrxml(loadFileHandlerJrxml);
        handlerRegistrations[5] = getView().addEndLoadHandlerDect(loadFileHandlerDect);
        handlerRegistrations[6] = getView().addJrxmlLoadHandlerDect(getJrxmlFileExistHandler(true));
        handlerRegistrations[7] = getView().addJrxmlLoadHandler(getJrxmlFileExistHandler(false));
    }

    private JrxmlFileExistEvent.JrxmlFileExistHandler getJrxmlFileExistHandler(final boolean isArchive){
        return new JrxmlFileExistEvent.JrxmlFileExistHandler() {
            @Override
            public void onJrxmlExist(final JrxmlFileExistEvent event) {
                LogAddEvent.fire(DeclarationTemplatePresenter.this, event.getErrorUuid());
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
                                                Dialog.infoMessage("Макет успешно обновлен");
                                                LogCleanEvent.fire(DeclarationTemplatePresenter.this);
                                                LogAddEvent.fire(DeclarationTemplatePresenter.this, result.getSuccessUuid(), true);
                                            }
                                        }, DeclarationTemplatePresenter.this));
                                    }
                                }, DeclarationTemplatePresenter.this));
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
}
