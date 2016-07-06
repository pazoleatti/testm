package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.*;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateMainUiHandlers;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client.event.CreateNewVersionEvent;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client.FormVersionHistoryPresenter;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.ArrayList;
import java.util.Date;


public class FormTemplateMainPresenter extends TabContainerPresenter<FormTemplateMainPresenter.MyView, FormTemplateMainPresenter.MyProxy>
		implements FormTemplateMainUiHandlers, FormTemplateSaveEvent.MyHandler, FormTemplateMainEvent.MyHandler,
        CreateNewVersionEvent.MyHandler{

	private HandlerRegistration closeFormTemplateHandlerRegistration;
	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;
	private FormTemplate formTemplate;
    private FormTemplateExt formTemplateExt;

    protected FormVersionHistoryPresenter versionHistoryPresenter;

    @Override
    @ProxyEvent
    public void onSetData(final FormTemplateMainEvent event) {
        InitTypeAction action = new InitTypeAction();
        formTemplateExt = new FormTemplateExt();
        formTemplate = new FormTemplate();
        formTemplateExt.setFormTemplate(formTemplate);
        formTemplate.setVersion(new Date());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<InitTypeResult>() {
            @Override
            public void onSuccess(InitTypeResult result) {
                FormTemplateSetEvent.fire(FormTemplateMainPresenter.this, formTemplateExt, result.getRefBookList());
            }
        }, FormTemplateMainPresenter.this));
        FormType type = new FormType();
        type.setId(0);
        type.setName("");
        type.setStatus(VersionedObjectStatus.DRAFT);
        type.setTaxType(event.getTaxType());
        formTemplate.setType(type);
        formTemplate.getStyles().addAll(new ArrayList<FormStyle>());
        getView().setFormId(0);
        getView().setTitle(formTemplate.getName());
        RevealContentEvent.fire(FormTemplateMainPresenter.this, RevealContentTypeHolder.getMainContent(), FormTemplateMainPresenter.this);

        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.formTemplateInfoPage).
                with(AdminConstants.NameTokens.formTemplateId, "0").build());
    }

    @Override
    @ProxyEvent
    public void onCreateVersion(CreateNewVersionEvent event) {
        GetFormTypeAction action = new GetFormTypeAction();
        action.setFormTypeId(event.getFormTypeId());

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetFormTypeResult>() {
                    @Override
                    public void onSuccess(GetFormTypeResult result) {
                        formTemplateExt = new FormTemplateExt();
                        formTemplate = new FormTemplate();
                        formTemplateExt.setFormTemplate(formTemplate);
                        formTemplate.setVersion(new Date());
                        formTemplate.setType(result.getFormType());
                        formTemplate.getStyles().addAll(new ArrayList<FormStyle>());
                        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.formTemplateInfoPage).
                                with(AdminConstants.NameTokens.formTemplateId, "0").build());
                        getView().setFormId(0);
                        getView().setTitle("");
                        TitleUpdateEvent.fire(FormTemplateMainPresenter.this, "Шаблон налоговой формы", formTemplate.getType().getName());
                        RevealContentEvent.fire(FormTemplateMainPresenter.this, RevealContentTypeHolder.getMainContent(), FormTemplateMainPresenter.this);
                        FormTemplateSetEvent.fire(FormTemplateMainPresenter.this, formTemplateExt, result.getRefBookList());
                    }
                }, this));
    }

    @ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateMainPage)
	public interface MyProxy extends ProxyPlace<FormTemplateMainPresenter>, Place {
	}

	public interface MyView extends TabView, HasUiHandlers<FormTemplateMainUiHandlers> {
		void setFormId(int formId);
		void setTitle(String title);
        void activateVersionName(String s);
        void activateVersion(boolean isVisible);
        void setLockInformation(boolean isVisible, String lockDate, String lockedBy);
    }

	@RequestTabs
	public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<RequestTabsHandler>();

	@ChangeTab
	public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<ChangeTabHandler>();

	@ContentSlot
	public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<RevealContentHandler<?>>();

	@Inject
	public FormTemplateMainPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy, DispatchAsync dispatcher, PlaceManager placeManager,
                                     FormVersionHistoryPresenter versionHistoryPresenter) {
		super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
        this.versionHistoryPresenter = versionHistoryPresenter;
	}

    @Override
    protected void onReveal() {
        super.onReveal();
        setFormTemplate();
        LogCleanEvent.fire(FormTemplateMainPresenter.this);
    }

    @Override
	public void reset() {
        super.onReset();
		setFormTemplate();
	}

	@Override
	public void onHide() {
		super.onHide();
        unlockForm((formTemplate != null && formTemplate.getId() != null)?formTemplate.getId():0);
        if (closeFormTemplateHandlerRegistration != null)
		    closeFormTemplateHandlerRegistration.removeHandler();
	}

	/**
	 * Сохраняет шаблон формы. Отправляет его на сервер.
	 *
	 */
	@Override
	public void save() {
		FormTemplateFlushEvent.fire(this);
		saveAfterFlush(false);
	}

	@ProxyEvent
	@Override
	public void onSave(FormTemplateSaveEvent event) {
		setFormTemplate();
	}

	/**
	 * Закрыть форму редактирования и вернуться на форму администрирования со списком шаблонов форм.
	 */
	@Override
	public void close() {
        if (formTemplate.getType().getId() == 0)
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.adminPage).build());
		else
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.formTemplateVersionList).
                with(AdminConstants.NameTokens.formTypeId, String.valueOf(formTemplate.getType().getId())).build());
	}

    /**
     * Изменение статуса у макета
     * @param force true - принудительно вызвает изменение статуса у макета,
     *              false - не меняет статус при наличии ошибок
     */
    @Override
    public void activate(boolean force) {
        SetStatusFormAction action = new SetStatusFormAction();
        action.setFormTemplateId(formTemplate.getId());
        action.setForce(force);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SetStatusFormResult>() {
            @Override
            public void onSuccess(SetStatusFormResult result) {
                LogAddEvent.fire(FormTemplateMainPresenter.this, result.getUuid());
                if (!result.isSetStatusSuccessfully()) { //
                    Dialog.confirmMessage("Информация",
                            "Найдены экземпляры " +
                                    (
                                            formTemplate.getType().getTaxType() == TaxType.DEAL
                                                    ||
                                            formTemplate.getType().getTaxType() == TaxType.ETR
                                                    ||
                                            formTemplate.getType().getTaxType() == TaxType.MARKET
                                                    ? "форм" : "налоговых форм"
                                    )
                                    + ", использующие версию макета. Изменить статус версии?",
                            new DialogHandler() {
                                @Override
                                public void yes() {
                                    activate(true);
                                    super.yes();
                                }
                            });
                } else {
                    formTemplate.setStatus(VersionedObjectStatus.getStatusById(result.getStatus()));
                    getView().activateVersionName(result.getStatus() == 0? "Вывести из действия" : "Ввести в действие");
                }
            }
        }, this));
    }

    private void setFormTemplate() {
        final int formId = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(AdminConstants.NameTokens.formTemplateId, "0"));
        if (formId != 0) {
			GetFormAction action = new GetFormAction();
			action.setId(formId);

			closeFormTemplateHandlerRegistration = Window.addWindowClosingHandler(new Window.ClosingHandler() {
				@Override
				public void onWindowClosing(Window.ClosingEvent event) {
					unlockForm(formId);
					closeFormTemplateHandlerRegistration.removeHandler();
				}
			});

			dispatcher.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<GetFormResult>() {
						@Override
						public void onSuccess(GetFormResult result) {
                            LogAddEvent.fire(FormTemplateMainPresenter.this, result.getUuid());
                            if (result.isLockedByAnotherUser()) {
                                getView().setLockInformation(true, result.getLockDate(), result.getLockedByUser());
                            } else {
                                getView().setLockInformation(false, null, null);
                            }
                            formTemplateExt = result.getForm();
							formTemplate = formTemplateExt.getFormTemplate();
							getView().setFormId(formTemplate.getId());
							getView().setTitle(formTemplate.getName());
                            getView().activateVersion(true);
                            getView().activateVersionName(formTemplate.getStatus().getId() == 0? "Вывести из действия" : "Ввести в действие");
							TitleUpdateEvent.fire(FormTemplateMainPresenter.this, "Шаблон налоговой формы", formTemplate.getType().getName());
							RevealContentEvent.fire(FormTemplateMainPresenter.this, RevealContentTypeHolder.getMainContent(), FormTemplateMainPresenter.this);
							FormTemplateSetEvent.fire(FormTemplateMainPresenter.this, result.getForm(), result.getRefBookList());
                            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.formTemplateInfoPage).
                                    with(AdminConstants.NameTokens.formTemplateId, String.valueOf(formId)).build());
						}
					}, this));
		} else {
            getView().activateVersion(false);
            getView().setLockInformation(false, null, null);
        }
        UpdateFTIdEvent.fire(FormTemplateMainPresenter.this, formId);
	}

	@SuppressWarnings("unchecked")
	private void unlockForm(int formId){
        if (formId == 0)
            return;
		UnlockFormAction action = new UnlockFormAction();
		action.setFormId(formId);
		dispatcher.execute(action, CallbackUtils.emptyCallback());
	}

	private void saveAfterFlush(boolean force) {
        if (formTemplate.getName().isEmpty() || formTemplate.getFullName().isEmpty()){
            Dialog.infoMessage("Не заполнено одно из обязательных полей. " +
                    "Проверьте поля \"Наименование формы\", \"Полное наименование формы\", \"Код формы\"");
            return;
        }
        if (formTemplateExt.getActualEndVersionDate() != null &&
                formTemplate.getVersion().compareTo(formTemplateExt.getActualEndVersionDate()) > 0 ){
            Dialog.infoMessage("Дата окончания не может быть меньше даты начала актуальности.");
            return;
        }
        for (FormStyle formStyle : formTemplate.getStyles()) {
            if (formStyle.getAlias() == null) {
                Dialog.infoMessage("Не заполнено обязательное поле \"Имя\" стиля на вкладке \"Наборы стилей\"");
                return;
            }
        }
        //Новый макет
        if (formTemplate.getId() == null && formTemplate.getType().getId() == 0){
            CreateNewTypeAction action = new CreateNewTypeAction();
            action.setForm(formTemplateExt.getFormTemplate());
            action.setVersionEndDate(formTemplateExt.getActualEndVersionDate());
            formTemplate.setStatus(VersionedObjectStatus.NORMAL);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CreateNewTypeResult>() {
                @Override
                public void onSuccess(CreateNewTypeResult result) {
                    LogAddEvent.fire(FormTemplateMainPresenter.this, result.getUuid());
                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.formTemplateInfoPage).
                            with(AdminConstants.NameTokens.formTemplateId, String.valueOf(result.getFormTemplateId())).build());
                    Dialog.infoMessage("Форма сохранена");
                    formTemplate.setId(result.getFormTemplateId());
                    formTemplate.setType(result.getFormType());
                    getView().activateVersion(true);
                    getView().activateVersionName(formTemplate.getStatus().getId() == 0? "Вывести из действия" : "Ввести в действие");
                    getView().setTitle(formTemplate.getName());
                    getView().setFormId(formTemplate.getId());
                    FormTemplateSetEvent.fire(FormTemplateMainPresenter.this, formTemplateExt, new ArrayList<RefBook>());
                    UpdateFTIdEvent.fire(FormTemplateMainPresenter.this, result.getFormTemplateId());
                }
            }, this));
        }
        //Новая версия макета
        else if (formTemplate.getId() == null){
            CreateNewVersionAction action = new CreateNewVersionAction();
            action.setForm(formTemplateExt.getFormTemplate());
            action.setVersionEndDate(formTemplateExt.getActualEndVersionDate());
            formTemplate.setStatus(VersionedObjectStatus.DRAFT);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<CreateNewVersionResult>() {
                @Override
                public void onSuccess(CreateNewVersionResult result) {
                    LogAddEvent.fire(FormTemplateMainPresenter.this, result.getUuid());
                    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.formTemplateInfoPage).
                            with(AdminConstants.NameTokens.formTemplateId, String.valueOf(result.getFormTemplateId())).build());
                    Dialog.infoMessage("Форма сохранена");
                    formTemplate.setId(result.getFormTemplateId());
                    getView().activateVersion(true);
                    getView().activateVersionName(formTemplate.getStatus().getId() == 0? "Вывести из действия" : "Ввести в действие");
                    getView().setTitle(formTemplate.getName());
                    getView().setFormId(formTemplate.getId());
                    //FormTemplateSetEvent.fire(FormTemplateMainPresenter.this, formTemplateExt, new ArrayList<RefBook>());
                    UpdateFTIdEvent.fire(FormTemplateMainPresenter.this, result.getFormTemplateId());
                    for (Column column : result.getColumns()){
                        Column col = formTemplate.getColumn(column.getAlias());
                        if (col.getId()==null || col.getId()<0){
                            if (col instanceof ReferenceColumn)
                                ((ReferenceColumn)col).setParentId(((ReferenceColumn)column).getParentId());
                            col.setId(column.getId());
                        }
                    }
                }
            }, this));
        }
        //Редактирование макета
        else {
            UpdateFormAction action = new UpdateFormAction();
            action.setForm(formTemplate);
            action.setVersionEndDate(formTemplateExt.getActualEndVersionDate());
            action.setForce(force);
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<UpdateFormResult>() {
                        @Override
                        public void onSuccess(UpdateFormResult result) {
                            LogCleanEvent.fire(FormTemplateMainPresenter.this);
                            if (result.isConfirmNeeded()) {
                                Dialog.confirmMessage("Информация",
                                        "Найдены экземпляры " +
                                                (
                                                        formTemplate.getType().getTaxType() == TaxType.DEAL
                                                        ||
                                                        formTemplate.getType().getTaxType() == TaxType.ETR
                                                        ||
                                                        formTemplate.getType().getTaxType() == TaxType.MARKET
                                                        ? "форм" : "налоговых форм"
                                                )
                                                + ", использующие версию макета. Продожить сохранение?",
                                        new DialogHandler() {
                                            @Override
                                            public void yes() {
                                                saveAfterFlush(true);
                                                super.yes();
                                            }
                                        });
                            } else {
                                LogAddEvent.fire(FormTemplateMainPresenter.this, result.getUuid());
                            }
                        }
                    }, this));
        }
    }

    @Override
    public void onReturnClicked() {
        if (formTemplate == null || formTemplate.getType().getId() == 0)
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.adminPage).build());
        else
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(AdminConstants.NameTokens.formTemplateVersionList).with(AdminConstants.NameTokens.formTypeId, String.valueOf(formTemplate.getType().getId())).build());
    }

    @Override
    public void onHistoryClicked() {
        Integer id = Integer.valueOf(placeManager.getCurrentPlaceRequest().getParameter(AdminConstants.NameTokens.formTemplateId, ""));
        if (id == 0)
            return;
        versionHistoryPresenter.init(id);
        addToPopupSlot(versionHistoryPresenter);
    }
}
