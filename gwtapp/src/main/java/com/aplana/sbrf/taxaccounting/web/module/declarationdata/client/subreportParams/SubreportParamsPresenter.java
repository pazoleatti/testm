package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesAction;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.shared.SaveLogEntriesResult;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.*;

/**
 * Презентор "Параметры печатной формы"
 */

public class SubreportParamsPresenter extends PresenterWidget<SubreportParamsPresenter.MyView> implements SubreportParamsUiHandlers {

    private DeclarationSubreport declarationSubreport;
    private HandlerRegistration closeFormDataHandlerRegistration;

    private final DispatchAsync dispatcher;
    private DeclarationDataPresenter declarationDataPresenter;

    private boolean selectRecord = false;

    public interface MyView extends PopupView, HasUiHandlers<SubreportParamsUiHandlers> {
		void setSubreport(DeclarationSubreport declarationSubreport, Map<Long, RefBookParamInfo> refBookParamInfoMap, Date startDate, Date endDate);
        Map<String, Object> getFieldsValues() throws BadValueException;
        void setTableData(PrepareSpecificReportResult prepareSpecificReportResult);
        DataRow<Cell> getSelectedRow();
    }

	@Inject
	public SubreportParamsPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
		super(eventBus, view);
        this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
        closeFormDataHandlerRegistration = Window.addCloseHandler(new CloseHandler<Window>() {
            @Override
            public void onClose(CloseEvent<Window> event) {
                closeFormDataHandlerRegistration.removeHandler();
            }
        });
        GetSubreportAction action = new GetSubreportAction();
        action.setDeclarationSubreportId(declarationSubreport.getId());
        action.setDeclarationId(declarationDataPresenter.getDeclarationId());
        getView().setTableData(null);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetSubreportResult>() {
                    @Override
                    public void onSuccess(GetSubreportResult result) {
                        if (!declarationDataPresenter.checkExistDeclarationData(result)) return;
                        selectRecord = declarationSubreport.isSelectRecord();
                        getView().setSubreport(declarationSubreport, result.getRefBookParamInfoMap(), result.getStartDate(), result.getEndDate());
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        super.onFailure(caught);
                    }
                }, this));

    }

    @Override
    public void onFind() {
        try {
            final PrepareSubreportAction action = new PrepareSubreportAction();
            action.setDeclarationDataId(declarationDataPresenter.getDeclarationId());
            action.setTaxType(declarationDataPresenter.getTaxType());
            action.setType(declarationSubreport.getAlias());
            action.setSubreportParamValues(getView().getFieldsValues());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<PrepareSubreportResult>() {
                        @Override
                        public void onSuccess(PrepareSubreportResult result) {
                            if (!declarationDataPresenter.checkExistDeclarationData(result)) return;
                            LogCleanEvent.fire(SubreportParamsPresenter.this);
                            LogAddEvent.fire(SubreportParamsPresenter.this, result.getUuid());
                            if (result.getPrepareSpecificReportResult() == null) {
                                getView().setTableData(null);
                            } else {
                                // отображение таблицы
                                getView().setTableData(result.getPrepareSpecificReportResult());
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            super.onFailure(caught);
                        }
                    }, this));
        } catch (BadValueException bve) {
            Dialog.errorMessage("Отчет не сформирован", "Обнаружены фатальные ошибки!");
            List<LogEntry> logEntries = new ArrayList<LogEntry>();
            for (String entry : bve){
                logEntries.add(new LogEntry(LogLevel.ERROR, entry));
            }

            SaveLogEntriesAction action = new SaveLogEntriesAction();
            action.setLogEntries(logEntries);

            dispatcher.execute(action,
                    CallbackUtils.defaultCallback(
                            new AbstractCallback<SaveLogEntriesResult>() {
                                @Override
                                public void onSuccess(SaveLogEntriesResult result) {
                                    LogAddEvent.fire(SubreportParamsPresenter.this, result.getUuid());
                                }
                            }, this));
        }
    }

    @Override
    public void onCreate() {
        try {
            CreateReportAction action = new CreateReportAction();
            action.setDeclarationDataId(declarationDataPresenter.getDeclarationId());
            action.setForce(false);
            action.setTaxType(declarationDataPresenter.getTaxType());
            action.setType(declarationSubreport.getAlias());
            action.setSubreportParamValues(getView().getFieldsValues());
            action.setCreate(true);
            if (selectRecord) {
                action.setSelectedRow(getView().getSelectedRow());
            }
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<CreateReportResult>() {
                        @Override
                        public void onSuccess(CreateReportResult result) {
                            if (!declarationDataPresenter.checkExistDeclarationData(result)) return;
                            LogCleanEvent.fire(SubreportParamsPresenter.this);
                            LogAddEvent.fire(SubreportParamsPresenter.this, result.getUuid());
                            if (CreateAsyncTaskStatus.NOT_EXIST_XML.equals(result.getStatus())) {
                                Dialog.infoMessage("Для текущего экземпляра не выполнен расчет.");
                            } else {
                                getView().hide();
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            super.onFailure(caught);
                        }
                    }, this));
        } catch (BadValueException bve) {
            Dialog.errorMessage("Отчет не сформирован", "Обнаружены фатальные ошибки!");
            List<LogEntry> logEntries = new ArrayList<LogEntry>();
            for (String entry : bve){
                logEntries.add(new LogEntry(LogLevel.ERROR, entry));
            }

            SaveLogEntriesAction action = new SaveLogEntriesAction();
            action.setLogEntries(logEntries);

            dispatcher.execute(action,
                    CallbackUtils.defaultCallback(
                            new AbstractCallback<SaveLogEntriesResult>() {
                                @Override
                                public void onSuccess(SaveLogEntriesResult result) {
                                    LogAddEvent.fire(SubreportParamsPresenter.this, result.getUuid());
                                }
                            }, this));
        }

    }

    public void setSubreport(DeclarationSubreport declarationSubreport) {
		this.declarationSubreport = declarationSubreport;
	}

    public void setDeclarationDataPresenter(DeclarationDataPresenter declarationDataPresenter) {
        this.declarationDataPresenter = declarationDataPresenter;
    }

    @Override
    public void onHide() {
        super.onHide();
        closeFormDataHandlerRegistration.removeHandler();
    }
}
