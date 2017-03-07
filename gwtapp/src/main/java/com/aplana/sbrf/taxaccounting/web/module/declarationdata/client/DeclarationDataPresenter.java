package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.DownloadUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.ParamUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.TaManualRevealCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments.DeclarationDeclarationFilesCommentsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.sources.SourcesPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams.SubreportParamsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeclarationDataPresenter
		extends
		Presenter<DeclarationDataPresenter.MyView, DeclarationDataPresenter.MyProxy>
		implements DeclarationDataUiHandlers {

	@ProxyCodeSplit
	@NameToken(DeclarationDataTokens.declarationData)
    public interface MyProxy extends ProxyPlace<DeclarationDataPresenter>, Place {}

    public static final String DECLARATION_UPDATE_MSG = "Область предварительного просмотра. Расчет налоговой формы выполнен.";
    public static final String DECLARATION_UPDATE_MSG_D = "Область предварительного просмотра. Расчет уведомления выполнен.";

    public static final String DECLARATION_DELETE_Q = "Вы уверены, что хотите удалить налоговую форму?";
    public static final String DECLARATION_DELETE_Q_D = "Вы уверены, что хотите удалить уведомление?";
    public static final String DECLARATION_DELETE_MSG = "Налоговая форма удалена";
    public static final String DECLARATION_DELETE_MSG_D = "Уведомление удалено";

    private static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");

	public interface MyView extends View,
			HasUiHandlers<DeclarationDataUiHandlers> {
		void showAccept(boolean show);

		void showReject(boolean show);

		void showRecalculateButton(boolean show);

        void showDownloadButtons(boolean show);

		void showDelete(boolean show);

		void setType(String type);

        void setFormKind(String formKype);

        void setDeclarationDataId(Long declarationDataId);

		void setTitle(String title, boolean isTaxTypeDeal);

		void setDepartment(String department);

		void setReportPeriod(String reportPeriod);

		void setBackButton(String link, String text);

		void setPdf(Pdf pdf);

		void setDocDate(Date date);

        void setKpp(String kpp);

        void setOktmo(String oktmo);

        void setTaxOrganCode(String taxOrganCode);

        void setStateED(String stateED);

        void setAsnuName(String asnuName);

        void setImportTf(String userName);

        void setFileName(String guid);

        void setPropertyBlockVisible(boolean isVisibleKpp, boolean isVisibleOktmo, boolean isVisibleTaxOrgan, boolean isVisibleStateED, boolean isVisibleAsnu, boolean isVisibleTf, TaxType taxType);

        void startTimerReport(DeclarationDataReportType type);

        void stopTimerReport(DeclarationDataReportType type);

        void updatePrintReportButtonName(DeclarationDataReportType type, boolean isLoad);

        void setPdfPage(int page);

        void showState(State state);

        void showNoPdf(String text);

        boolean getVisiblePdfViewer();

        void setSubreports(List<DeclarationSubreport> subreports);

        void updatePrintSubreportButtonName(DeclarationSubreport subreport, boolean exist);

        void setVisiblePDF(boolean isVisiblePDF);

        boolean isVisiblePDF();
    }

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;
	private final DialogPresenter dialogPresenter;
	private final HistoryPresenter historyPresenter;
    private final SubreportParamsPresenter subreportParamsPresenter;
    private final DeclarationDeclarationFilesCommentsPresenter declarationFilesCommentsPresenter;
	private long declarationId;
    private DeclarationData declarationData;
    private String taxName;
    private TaxType taxType;
    private final SourcesPresenter sourcesPresenter;
    private List<DeclarationSubreport> subreports = new ArrayList<DeclarationSubreport>();

	@Inject
	public DeclarationDataPresenter(final EventBus eventBus, final MyView view,
									final MyProxy proxy, DispatchAsync dispatcher,
									PlaceManager placeManager, DialogPresenter dialogPresenter,
									HistoryPresenter historyPresenter, SubreportParamsPresenter subreportParamsPresenter,
                                    SourcesPresenter sourcesPresenter, DeclarationDeclarationFilesCommentsPresenter declarationFilesCommentsPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.historyPresenter = historyPresenter;
		this.placeManager = (TaPlaceManager) placeManager;
		this.dialogPresenter = dialogPresenter;
        this.sourcesPresenter = sourcesPresenter;
        this.subreportParamsPresenter = subreportParamsPresenter;
        this.declarationFilesCommentsPresenter = declarationFilesCommentsPresenter;
		getView().setUiHandlers(this);
	}

	/**
	 * Здесь происходит подготовка декларации.
	 * 
	 * @param request
	 *            запрос
	 */
	@Override
	public void prepareFromRequest(final PlaceRequest request) {
		super.prepareFromRequest(request);
		final long id = ParamUtils.getLong(request,
				DeclarationDataTokens.declarationId);

		GetDeclarationDataAction action = new GetDeclarationDataAction();
		action.setId(id);
		dispatcher.execute(
				action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetDeclarationDataResult>() {
							@Override
							public void onSuccess(
									GetDeclarationDataResult result) {
                                LogAddEvent.fire(DeclarationDataPresenter.this, result.getUuid());
								declarationId = id;
                                declarationData = result.getDeclarationData();
								taxName = result.getTaxType().name();
                                taxType = result.getTaxType();
                                //sourcesPresenter.setTaxType(taxType);
                                getView().setType(result.getDeclarationFormType());
                                getView().setFormKind(result.getDeclarationFormKind().getTitle());
                                getView().setDeclarationDataId(declarationData.getId());
                                String periodStr = result.getReportPeriodYear() + ", " + result.getReportPeriod();
                                if (result.getCorrectionDate() != null) {
                                    periodStr += ", корр. (" + DATE_TIME_FORMAT.format(result.getCorrectionDate()) + ")";
                                }
								getView().setReportPeriod(periodStr);
								getView().setDocDate(result.getDocDate());
                                getView().setDepartment(result.getDepartment());
                                getView().setVisiblePDF(result.isVisiblePDF());
                                subreports = result.getSubreports();
                                getView().setSubreports(result.getSubreports());
                                if (taxType.equals(TaxType.NDFL)){
                                    if (DeclarationFormKind.REPORTS.equals(result.getDeclarationFormKind())) {
                                        getView().setPropertyBlockVisible(true, true, true, result.getStateEDName() != null, false, false, taxType);
                                        getView().setKpp(declarationData.getKpp());
                                        getView().setOktmo(declarationData.getOktmo());
                                        getView().setTaxOrganCode(declarationData.getTaxOrganCode());
                                        getView().setStateED(result.getStateEDName());
                                        getView().setImportTf(result.getUserLoginImportTf());
                                    } else {
                                        if (result.getAsnuName() != null && !result.getAsnuName().isEmpty()) {
                                            getView().setPropertyBlockVisible(false, false, false, false, true, true, taxType);
                                            getView().setAsnuName(result.getAsnuName());
                                            getView().setFileName(result.getFileName());
                                            getView().setImportTf(result.getUserLoginImportTf());
                                        } else {
                                            getView().setImportTf(result.getUserLoginImportTf());
                                            getView().setPropertyBlockVisible(false, false, false, false, false, true, taxType);
                                        }
                                    }
                                } else {
                                    getView().setImportTf(result.getUserLoginImportTf());
                                    getView().setPropertyBlockVisible(false, false, false, false, false, true, taxType);
                                }
								getView()
										.setBackButton(
												"#"
														+ DeclarationListNameTokens.DECLARATION_LIST
														+ ";nType="
														+ result.getTaxType()
                                                        + (TaxType.NDFL.equals(taxType) && DeclarationFormKind.REPORTS.equals(result.getDeclarationFormKind())?(";"+ DeclarationListPresenter.REPORTS + "=" + true):""), result.getTaxType()
                                        .getName());
								getView().setTitle(result.getDeclarationType(), result.getTaxType().equals(TaxType.DEAL));
								updateTitle(result.getDeclarationType());

                                getView().setPdfPage(0);
                                getView().showState(declarationData.getState());
								getView().showAccept(result.isCanAccept());
								getView().showReject(result.isCanReject());
								getView().showDelete(result.isCanDelete());
								getView().showRecalculateButton(result.isCanDelete());

                                onTimerReport(DeclarationDataReportType.XML_DEC, false);
                                onTimerReport(DeclarationDataReportType.EXCEL_DEC, false);
							}
						}, DeclarationDataPresenter.this).addCallback(
                        TaManualRevealCallback.create(
                                DeclarationDataPresenter.this, placeManager)));

	}

    @Override
    public void onTimerReport(final DeclarationDataReportType type, final boolean isTimer) {
        TimerReportAction action = new TimerReportAction();
        action.setDeclarationDataId(declarationId);
        action.setType(type.getReportAlias());
        dispatcher.execute(action, CallbackUtils
                .simpleCallback(new AbstractCallback<TimerReportResult>() {
                    @Override
                    public void onSuccess(TimerReportResult result) {
                        if (DeclarationDataReportType.PDF_DEC.equals(type) && result.getExistXMLReport() != null) {
                            //перезапуск таймера XML, выполняется если был запущен таймер ожидания PDF при этом нету XML
                            getView().stopTimerReport(type);
                            onTimerReport(DeclarationDataReportType.XML_DEC, false);
                        } else if (result.getExistReport().equals(TimerReportResult.StatusReport.EXIST)) {
                            getView().updatePrintReportButtonName(type, true);
                            if (DeclarationDataReportType.XML_DEC.equals(type)) {
                                onTimerReport(DeclarationDataReportType.PDF_DEC, false);
                                onTimerReport(DeclarationDataReportType.EXCEL_DEC, false);
                            } else if (DeclarationDataReportType.PDF_DEC.equals(type)) {
                                if (getView().isVisiblePDF()) {
                                    getView().showNoPdf("Загрузка формы предварительного просмотра");
                                    getPdf();
                                } else {
                                    getView().showNoPdf((!TaxType.DEAL.equals(taxType) ? DECLARATION_UPDATE_MSG : DECLARATION_UPDATE_MSG_D) +
                                            " Форма предварительного просмотра недоступна");
                                }
                            }
                        } else if (result.getExistReport().equals(TimerReportResult.StatusReport.NOT_EXIST)) { // если файл не существует и нет блокировки(т.е. задачу отменили или ошибка при формировании)
                            getView().stopTimerReport(type);
                            if (DeclarationDataReportType.XML_DEC.equals(type)) {
                                getView().showNoPdf("Область предварительного просмотра");
                            } else if (DeclarationDataReportType.PDF_DEC.equals(type)) {
                                if (getView().isVisiblePDF()) {
                                    getView().showNoPdf((!TaxType.DEAL.equals(taxType) ? DECLARATION_UPDATE_MSG : DECLARATION_UPDATE_MSG_D) +
                                            " Форма предварительного просмотра не сформирована");
                                } else {
                                    getView().showNoPdf((!TaxType.DEAL.equals(taxType) ? DECLARATION_UPDATE_MSG : DECLARATION_UPDATE_MSG_D) +
                                            " Форма предварительного просмотра недоступна");
                                }
                            }
                            getView().updatePrintReportButtonName(type, false);
                        } else if (result.getExistReport().equals(TimerReportResult.StatusReport.LIMIT)) {
                            getView().stopTimerReport(type);
                            if (DeclarationDataReportType.PDF_DEC.equals(type)) {
                                getView().showNoPdf((!TaxType.DEAL.equals(taxType) ? DECLARATION_UPDATE_MSG : DECLARATION_UPDATE_MSG_D) +
                                        "  Форма предварительного просмотра недоступна");
                            } else if (DeclarationDataReportType.XML_DEC.equals(type)) {
                                getView().showNoPdf("Область предварительного просмотра");
                            }
                            getView().updatePrintReportButtonName(type, false);
                        } else if (!isTimer) {  //Если задача на формирование уже запущена, то переходим в режим ожидания
                            if (DeclarationDataReportType.XML_DEC.equals(type)) {
                                getView().showNoPdf(!TaxType.DEAL.equals(taxType)?"Заполнение налоговой формы данными":"Заполнение уведомления данными");
                            } else if (DeclarationDataReportType.PDF_DEC.equals(type)) {
                                getView().showNoPdf((!TaxType.DEAL.equals(taxType)?DECLARATION_UPDATE_MSG:DECLARATION_UPDATE_MSG_D) +
                                        " Идет формирование формы предварительного просмотра");
                            }
                            getView().updatePrintReportButtonName(type, false);
                            getView().startTimerReport(type);
                        }
                    }
                }));
    }

    @Override
    public void onTimerSubsreport(final boolean isTimer) {
        TimerSubreportAction action = new TimerSubreportAction();
        action.setDeclarationDataId(declarationId);
        dispatcher.execute(action, CallbackUtils
                .simpleCallback(new AbstractCallback<TimerSubreportResult>() {
                    @Override
                    public void onSuccess(TimerSubreportResult result) {
                        for(DeclarationSubreport subreport: subreports) {
                            TimerSubreportResult.StatusReport status = result.getMapExistReport().get(subreport.getAlias());
                            if (status != null) {
                                switch (status) {
                                    case EXIST:
                                        getView().updatePrintSubreportButtonName(subreport, true);
                                        break;
                                    case LOCKED:
                                    case NOT_EXIST:
                                        getView().updatePrintSubreportButtonName(subreport, false);
                                        break;
                                }
                            }
                        }
                    }
                }));
    }


    @Override
    public void onOpenSourcesDialog() {
        sourcesPresenter.setDeclarationId(declarationId);
        addToPopupSlot(sourcesPresenter);
    }

    @Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void onRecalculateClicked(final Date docDate, final boolean force, final boolean cancelTask) {
		LogCleanEvent.fire(this);
        getView().showNoPdf(!TaxType.DEAL.equals(taxType)?"Заполнение налоговой формы данными":"Заполнение уведомления данными");
        RecalculateDeclarationDataAction action = new RecalculateDeclarationDataAction();
		action.setDeclarationId(declarationId);
		action.setDocDate(docDate);
        action.setTaxType(taxType);
        action.setForce(force);
        action.setCancelTask(cancelTask);
		dispatcher
				.execute(
						action,
						CallbackUtils
								.defaultCallback(new AbstractCallback<RecalculateDeclarationDataResult>() {
									@Override
									public void onSuccess(
											RecalculateDeclarationDataResult result) {
                                        LogAddEvent.fire(DeclarationDataPresenter.this, result.getUuid());
                                        if (CreateAsyncTaskStatus.LOCKED.equals(result.getStatus()) && !force) {
                                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                                @Override
                                                public void yes() {
                                                    onRecalculateClicked(docDate, true, cancelTask);
                                                }
                                            });
                                        } else if (CreateAsyncTaskStatus.EXIST_TASK.equals(result.getStatus()) && !cancelTask) {
                                            Dialog.confirmMessage(LockData.RESTART_LINKED_TASKS_MSG, new DialogHandler() {
                                                @Override
                                                public void yes() {
                                                    onRecalculateClicked(docDate, force, true);
                                                }
                                            });
                                        }
                                        onTimerReport(DeclarationDataReportType.XML_DEC, false);
									}

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        super.onFailure(caught);
                                        onTimerReport(DeclarationDataReportType.EXCEL_DEC, false);
                                        onTimerReport(DeclarationDataReportType.XML_DEC, false);
                                    }
                                }, DeclarationDataPresenter.this));
	}

	@Override
	public void accept(boolean accepted, final boolean force, final boolean cancelTask) {
		if (accepted) {
			LogCleanEvent.fire(this);
			AcceptDeclarationDataAction action = new AcceptDeclarationDataAction();
			action.setAccepted(true);
			action.setDeclarationId(declarationId);
            action.setForce(force);
            action.setTaxType(taxType);
            action.setCancelTask(cancelTask);
			dispatcher
					.execute(
							action,
							CallbackUtils
									.defaultCallback(new AbstractCallback<AcceptDeclarationDataResult>() {
										@Override
										public void onSuccess(
												AcceptDeclarationDataResult result) {
                                            LogAddEvent.fire(DeclarationDataPresenter.this, result.getUuid());
                                            if (CreateAsyncTaskStatus.NOT_EXIST_XML.equals(result.getStatus())) {
                                                Dialog.infoMessage("Для текущего экземпляра " + taxType.getDeclarationShortName() + " не выполнен расчет. " + DeclarationDataReportType.ACCEPT_DEC.getReportType().getDescription().replaceAll("\\%s", taxType.getDeclarationShortName()) + " невозможно");
                                            } else if (CreateAsyncTaskStatus.LOCKED.equals(result.getStatus()) && !force) {
                                                Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                                    @Override
                                                    public void yes() {
                                                        accept(true, true, cancelTask);
                                                    }
                                                });
                                            } else if (CreateAsyncTaskStatus.EXIST_TASK.equals(result.getStatus()) && !cancelTask) {
                                                Dialog.confirmMessage(LockData.RESTART_LINKED_TASKS_MSG, new DialogHandler() {
                                                    @Override
                                                    public void yes() {
                                                        accept(true, force, true);
                                                    }
                                                });
                                            }
                                            onTimerReport(DeclarationDataReportType.ACCEPT_DEC, false);
										}
									}, DeclarationDataPresenter.this));
		} else {
            Dialog.confirmMessageYesClose("Возврат в Создана", "Вы действительно хотите вернуть в статус \"Создана\" форму?\"", new DialogHandler() {
                @Override
                public void yes() {
                    LogCleanEvent.fire(DeclarationDataPresenter.this);
                    AcceptDeclarationDataAction action = new AcceptDeclarationDataAction();
                    action.setAccepted(false);
                    action.setDeclarationId(declarationId);
                    dispatcher.execute(action, CallbackUtils
                            .defaultCallback(new AbstractCallback<AcceptDeclarationDataResult>() {
                                @Override
                                public void onSuccess(AcceptDeclarationDataResult result) {
                                    revealPlaceRequest();
                                }
                            }, DeclarationDataPresenter.this));
                }
            });
            //dialogPresenter.setDeclarationId(declarationId);
			//addToPopupSlot(dialogPresenter);
		}

	}

	@Override
	public void delete() {
        final DeclarationDataPresenter t = this;
        Dialog.confirmMessage(!taxType.equals(TaxType.DEAL) ? DECLARATION_DELETE_Q : DECLARATION_DELETE_Q_D, new DialogHandler() {
            @Override
            public void yes() {
                LogCleanEvent.fire(t);
                DeleteDeclarationDataAction action = new DeleteDeclarationDataAction();
                action.setDeclarationId(declarationId);
                dispatcher
                        .execute(
                                action,
                                CallbackUtils
                                        .defaultCallback(new AbstractCallback<DeleteDeclarationDataResult>() {
                                            @Override
                                            public void onSuccess(
                                                    DeleteDeclarationDataResult result) {
                                                MessageEvent
                                                        .fire(DeclarationDataPresenter.this,
                                                                !taxType.equals(TaxType.DEAL) ? DECLARATION_DELETE_MSG : DECLARATION_DELETE_MSG_D);
                                                placeManager
                                                        .revealPlace(new PlaceRequest(
                                                                DeclarationListNameTokens.DECLARATION_LIST)
                                                                .with("nType",
                                                                        taxName));
                                            }
                                        }, DeclarationDataPresenter.this));
                Dialog.hideMessage();
            }

            @Override
            public void no() {
                Dialog.hideMessage();
            }

            @Override
            public void close() {
                Dialog.hideMessage();
            }
        });
	}

	@Override
	public void check(final boolean force) {
		LogCleanEvent.fire(this);
		CheckDeclarationDataAction checkAction = new CheckDeclarationDataAction();
		checkAction.setDeclarationId(declarationId);
        checkAction.setTaxType(taxType);
        checkAction.setForce(force);
		dispatcher.execute(checkAction, CallbackUtils
				.defaultCallback(new AbstractCallback<CheckDeclarationDataResult>() {
					@Override
					public void onSuccess(CheckDeclarationDataResult result) {
                        LogCleanEvent.fire(DeclarationDataPresenter.this);
                        LogAddEvent.fire(DeclarationDataPresenter.this, result.getUuid());
                        if (CreateAsyncTaskStatus.NOT_EXIST_XML.equals(result.getStatus())) {
                            Dialog.infoMessage("Для текущего экземпляра " + taxType.getDeclarationShortName() + " не выполнен расчет. " + DeclarationDataReportType.CHECK_DEC.getReportType().getDescription().replaceAll("\\%s", "данных") + " невозможна");
                        } else if (CreateAsyncTaskStatus.LOCKED.equals(result.getStatus()) && !force) {
                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                @Override
                                public void yes() {
                                    check(true);
                                }
                            });
                        }
                        onTimerReport(DeclarationDataReportType.CHECK_DEC, false);
                    }
				}, this));
	}

	@Override
	public void downloadXml() {
		Window.open(GWT.getHostPageBaseURL() + "download/declarationData/xml/"
				+ declarationId, null, null);
	}

	@Override
	public void onInfoClicked() {
		historyPresenter.prepareDeclarationHistory(declarationId, taxType);
		addToPopupSlot(historyPresenter);
	}

    @Override
    public void revealPlaceRequest() {
		placeManager.revealPlace(new PlaceRequest(
				DeclarationDataTokens.declarationData).with(
				DeclarationDataTokens.declarationId,
				String.valueOf(declarationId)));
	}

	private void updateTitle(String declarationType) {
		TitleUpdateEvent.fire(this, "Налоговая форма", declarationType);
	}

    @Override
    public TaxType getTaxType() {
        return taxType;
    }

    public long getDeclarationId() {
        return declarationId;
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().stopTimerReport(DeclarationDataReportType.ACCEPT_DEC);
        getView().stopTimerReport(DeclarationDataReportType.XML_DEC);
        getView().stopTimerReport(DeclarationDataReportType.PDF_DEC);
        getView().stopTimerReport(DeclarationDataReportType.EXCEL_DEC);
        getView().stopTimerReport(new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, null));
    }

    @Override
    public void viewReport(final boolean force, final DeclarationDataReportType type) {
        if (type.isSubreport() && !type.getSubreport().getDeclarationSubreportParams().isEmpty()) {
            // Открываем форму со списком параметров для спец. отчета
            subreportParamsPresenter.setSubreport(type.getSubreport());
            subreportParamsPresenter.setDeclarationDataPresenter(this);
            addToPopupSlot(subreportParamsPresenter);
        } else {
            CreateReportAction action = new CreateReportAction();
            action.setDeclarationDataId(declarationId);
            action.setForce(force);
            action.setTaxType(taxType);
            action.setType(type.getReportAlias());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<CreateReportResult>() {
                        @Override
                        public void onSuccess(CreateReportResult result) {
                            LogCleanEvent.fire(DeclarationDataPresenter.this);
                            LogAddEvent.fire(DeclarationDataPresenter.this, result.getUuid());
                            if (CreateAsyncTaskStatus.NOT_EXIST_XML.equals(result.getStatus())) {
                                Dialog.infoMessage("Для текущего экземпляра " + taxType.getDeclarationShortName() + " не выполнен расчет. " + DeclarationDataReportType.PDF_DEC.getReportType().getDescription().replaceAll("\\%s", taxType.getDeclarationShortName()) + " невозможно");
                            } else if (CreateAsyncTaskStatus.LOCKED.equals(result.getStatus()) && !force) {
                                Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                    @Override
                                    public void yes() {
                                        viewReport(true, type);
                                    }
                                });
                            } else if (CreateAsyncTaskStatus.EXIST.equals(result.getStatus())) {
                                switch (type.getReportType()) {
                                    case EXCEL_DEC:
                                        DownloadUtils.openInIframe(GWT.getHostPageBaseURL() + "download/declarationData/xlsx/"
                                                + declarationId);
                                        break;
                                    case SPECIFIC_REPORT_DEC:
                                        DownloadUtils.openInIframe(GWT.getHostPageBaseURL() + "download/declarationData/specific/"
                                                + type.getSubreport().getAlias() + "/" + declarationId);
                                        break;
                                    default:
                                        break;
                                }
                                //DeclarationDataReportType.EXCEL_DEC.equals(type))
                            }
                            if (!type.isSubreport())
                                onTimerReport(type, false);
                            else
                                onTimerSubsreport(false);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            super.onFailure(caught);
                            if (type.getReportType().equals(ReportType.SPECIFIC_REPORT_DEC))
                                onTimerReport(type, false);
                            else
                                onTimerSubsreport(false);
                        }
                    }, this));
        }
    }

    private void getPdf() {
        GetPdfAction action = new GetPdfAction();
        action.setDeclarationDataId(declarationId);
        dispatcher.execute(action, CallbackUtils
                .defaultCallbackNoLock(new AbstractCallback<GetPdfResult>() {
                    @Override
                    public void onSuccess(GetPdfResult result) {
                        getView().setPdf(result.getPdf());
                    }
                }, this));
    }

    @Override
    public void onFilesCommentsDialog() {
        declarationFilesCommentsPresenter.setFormData(declarationData);
        addToPopupSlot(declarationFilesCommentsPresenter);
    }

}
