package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.sources.SourcesPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListNameTokens;
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

import java.util.Date;

public class DeclarationDataPresenter
		extends
		Presenter<DeclarationDataPresenter.MyView, DeclarationDataPresenter.MyProxy>
		implements DeclarationDataUiHandlers {

	@ProxyCodeSplit
	@NameToken(DeclarationDataTokens.declarationData)
    public interface MyProxy extends ProxyPlace<DeclarationDataPresenter>, Place {}

    public static final String DECLARATION_UPDATE_MSG = "Область предварительного просмотра. Расчет декларации выполнен.";
    public static final String DECLARATION_UPDATE_MSG_D = "Область предварительного просмотра. Расчет уведомления выполнен.";

    public static final String DECLARATION_DELETE_Q = "Вы уверены, что хотите удалить декларацию?";
    public static final String DECLARATION_DELETE_Q_D = "Вы уверены, что хотите удалить уведомление?";
    public static final String DECLARATION_DELETE_MSG = "Декларация удалена";
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

		void setTitle(String title, boolean isTaxTypeDeal);

		void setDepartment(String department);

		void setReportPeriod(String reportPeriod);

		void setBackButton(String link, String text);

		void setPdf(Pdf pdf);

		void setDocDate(Date date);

        void setTaxOrganCode(String taxOrganCode);

        void setKpp(String kpp);

        void setPropertyBlockVisible(boolean isVisibleTaxOrgan, boolean isVisibleKpp, TaxType taxType);

        void startTimerReport(ReportType reportType);

        void stopTimerReport(ReportType reportType);

        void updatePrintReportButtonName(ReportType reportType, boolean isLoad);

        void setPdfPage(int page);

        void showState(boolean accepted);

        void showNoPdf(String text);

        boolean getVisiblePdfViewer();
    }

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;
	private final DialogPresenter dialogPresenter;
	private final HistoryPresenter historyPresenter;
	private long declarationId;
	private String taxName;
    private TaxType taxType;
    private final SourcesPresenter sourcesPresenter;

	@Inject
	public DeclarationDataPresenter(final EventBus eventBus, final MyView view,
									final MyProxy proxy, DispatchAsync dispatcher,
									PlaceManager placeManager, DialogPresenter dialogPresenter,
									HistoryPresenter historyPresenter,
                                    SourcesPresenter sourcesPresenter) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.historyPresenter = historyPresenter;
		this.placeManager = (TaPlaceManager) placeManager;
		this.dialogPresenter = dialogPresenter;
        this.sourcesPresenter = sourcesPresenter;
		getView().setUiHandlers(this);
	}

	/**
	 * Здесь происходит подготовка декларации.
	 * 
	 * @param request
	 *            запрос
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
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
								taxName = result.getTaxType().name();
                                taxType = result.getTaxType();
                                sourcesPresenter.setTaxType(taxType);
								if (!taxType.equals(TaxType.DEAL)) {
                                    getView().setType("Декларация");
                                } else {
                                    getView().setType("Уведомление");
                                }
                                String periodStr = result.getReportPeriodYear() + ", " + result.getReportPeriod();
                                if (result.getCorrectionDate() != null) {
                                    periodStr += ", корр. (" + DATE_TIME_FORMAT.format(result.getCorrectionDate()) + ")";
                                }
								getView().setReportPeriod(periodStr);
								getView().setDocDate(result.getDocDate());
                                getView().setDepartment(result.getDepartment());
                                if(taxType.equals(TaxType.PROPERTY) || taxType.equals(TaxType.TRANSPORT)){
                                    getView().setPropertyBlockVisible(true, true, taxType);
                                    getView().setTaxOrganCode(result.getTaxOrganCode());
                                    getView().setKpp(result.getKpp());
                                } else if (taxType.equals(TaxType.INCOME)){
                                    getView().setPropertyBlockVisible(false, true, taxType);
                                    getView().setKpp(result.getKpp());
                                } else {
                                    getView().setPropertyBlockVisible(false, false, taxType);
                                }
								getView()
										.setBackButton(
												"#"
														+ DeclarationListNameTokens.DECLARATION_LIST
														+ ";nType="
														+ result.getTaxType(), result.getTaxType()
                                        .getName());
								getView().setTitle(result.getDeclarationType(), result.getTaxType().equals(TaxType.DEAL));
								updateTitle(result.getDeclarationType());

                                getView().setPdfPage(0);
                                getView().showState(result.isAccepted());
								getView().showAccept(result.isCanAccept());
								getView().showReject(result.isCanReject());
								getView().showDelete(result.isCanDelete());
								getView().showRecalculateButton(result.isCanDelete());

                                onTimerReport(ReportType.XML_DEC, false);
                                onTimerReport(ReportType.EXCEL_DEC, false);
							}
						}, DeclarationDataPresenter.this).addCallback(
						TaManualRevealCallback.create(
								DeclarationDataPresenter.this, placeManager)));

	}

    @Override
    public void onTimerReport(final ReportType reportType, final boolean isTimer) {
        TimerReportAction action = new TimerReportAction();
        action.setDeclarationDataId(declarationId);
        action.setType(reportType);
        dispatcher.execute(action, CallbackUtils
                .simpleCallback(new AbstractCallback<TimerReportResult>() {
                    @Override
                    public void onSuccess(TimerReportResult result) {
                        if (ReportType.PDF_DEC.equals(reportType) && result.getExistXMLReport() != null) {
                            //перезапуск таймера XML, выполняется если был запущен таймер ожидания PDF при этом нету XML
                            getView().stopTimerReport(reportType);
                            onTimerReport(ReportType.XML_DEC, false);
                        } else if (result.getExistReport().equals(TimerReportResult.StatusReport.EXIST)) {
                            getView().updatePrintReportButtonName(reportType, true);
                            if (ReportType.XML_DEC.equals(reportType)) {
                                onTimerReport(ReportType.PDF_DEC, false);
                                onTimerReport(ReportType.EXCEL_DEC, false);
                            } else if (ReportType.PDF_DEC.equals(reportType)) {
                                getView().showNoPdf("Загрузка формы предварительного просмотра");
                                getPdf();
                            }
                        } else if (result.getExistReport().equals(TimerReportResult.StatusReport.NOT_EXIST)) { // если файл не существует и нет блокировки(т.е. задачу отменили или ошибка при формировании)
                            getView().stopTimerReport(reportType);
                            if (ReportType.XML_DEC.equals(reportType)) {
                                getView().showNoPdf("Область предварительного просмотра");
                            } else if (ReportType.PDF_DEC.equals(reportType)) {
                                getView().showNoPdf((!TaxType.DEAL.equals(taxType)?DECLARATION_UPDATE_MSG:DECLARATION_UPDATE_MSG_D) +
                                        " Форма предварительного просмотра не сформирована");
                            }
                            getView().updatePrintReportButtonName(reportType, false);
                        } else if (result.getExistReport().equals(TimerReportResult.StatusReport.LIMIT)) {
                            getView().stopTimerReport(reportType);
                            if (ReportType.PDF_DEC.equals(reportType)) {
                                getView().showNoPdf((!TaxType.DEAL.equals(taxType) ? DECLARATION_UPDATE_MSG : DECLARATION_UPDATE_MSG_D) +
                                        "  Форма предварительного просмотра недоступна");
                            } else if (ReportType.XML_DEC.equals(reportType)) {
                                getView().showNoPdf("Область предварительного просмотра");
                            }
                            getView().updatePrintReportButtonName(reportType, false);
                        } else if (!isTimer) {  //Если задача на формирование уже запущена, то переходим в режим ожидания
                            if (ReportType.XML_DEC.equals(reportType)) {
                                getView().showNoPdf(!TaxType.DEAL.equals(taxType)?"Заполнение декларации данными":"Заполнение уведомления данными");
                            } else if (ReportType.PDF_DEC.equals(reportType)) {
                                getView().showNoPdf((!TaxType.DEAL.equals(taxType)?DECLARATION_UPDATE_MSG:DECLARATION_UPDATE_MSG_D) +
                                        " Идет формирование формы предварительного просмотра");
                            }
                            getView().updatePrintReportButtonName(reportType, false);
                            getView().startTimerReport(reportType);
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
        getView().showNoPdf(!TaxType.DEAL.equals(taxType)?"Заполнение декларации данными":"Заполнение уведомления данными");
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
                                        onTimerReport(ReportType.XML_DEC, false);
									}

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        super.onFailure(caught);
                                        onTimerReport(ReportType.EXCEL_DEC, false);
                                        onTimerReport(ReportType.XML_DEC, false);
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
                                                Dialog.infoMessage("Для текущего экземпляра " + taxType.getDeclarationShortName() + " не выполнен расчет. " + ReportType.ACCEPT_DEC.getDescription().replaceAll("\\%s", taxType.getDeclarationShortName()) + " невозможно");
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
                                            onTimerReport(ReportType.ACCEPT_DEC, false);
										}
									}, DeclarationDataPresenter.this));
		} else {
			dialogPresenter.setDeclarationId(declarationId);
			addToPopupSlot(dialogPresenter);
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
                            Dialog.infoMessage("Для текущего экземпляра " + taxType.getDeclarationShortName() + " не выполнен расчет. " + ReportType.CHECK_DEC.getDescription().replaceAll("\\%s", "данных") + " невозможна");
                        } else if (CreateAsyncTaskStatus.LOCKED.equals(result.getStatus()) && !force) {
                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                @Override
                                public void yes() {
                                    check(true);
                                }
                            });
                        }
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
		TitleUpdateEvent.fire(this, "Декларация", declarationType);
	}

    @Override
    public TaxType getTaxType() {
        return taxType;
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().stopTimerReport(ReportType.XML_DEC);
        getView().stopTimerReport(ReportType.PDF_DEC);
        getView().stopTimerReport(ReportType.EXCEL_DEC);
    }

    @Override
    public void viewReport(final boolean force, final ReportType reportType) {
        CreateReportAction action = new CreateReportAction();
        action.setDeclarationDataId(declarationId);
        action.setForce(force);
        action.setTaxType(taxType);
        action.setReportType(reportType);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CreateReportResult>() {
                    @Override
                    public void onSuccess(CreateReportResult result) {
                        LogCleanEvent.fire(DeclarationDataPresenter.this);
                        LogAddEvent.fire(DeclarationDataPresenter.this, result.getUuid());
                        if (CreateAsyncTaskStatus.NOT_EXIST_XML.equals(result.getStatus())) {
                            Dialog.infoMessage("Для текущего экземпляра " + taxType.getDeclarationShortName() + " не выполнен расчет. " + ReportType.PDF_DEC.getDescription().replaceAll("\\%s", taxType.getDeclarationShortName()) + " невозможно");
                        } else if (CreateAsyncTaskStatus.LOCKED.equals(result.getStatus()) && force == false) {
                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                @Override
                                public void yes() {
                                    viewReport(true, reportType);
                                }
                            });
                        } else if (CreateAsyncTaskStatus.EXIST.equals(result.getStatus()) &&
                                ReportType.EXCEL_DEC.equals(reportType)) {
                            DownloadUtils.openInIframe(GWT.getHostPageBaseURL() + "download/declarationData/xlsx/"
                                    + declarationId);
                        }
                        onTimerReport(reportType, false);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        super.onFailure(caught);
                        onTimerReport(reportType, false);
                    }
                }, this));
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
}
