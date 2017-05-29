package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.PrepareSpecificReportResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.AbstractBadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.WarnValueException;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Презентор "Параметры печатной формы"
 */

public class SubreportParamsPresenter extends PresenterWidget<SubreportParamsPresenter.MyView> implements SubreportParamsUiHandlers {

    private DeclarationSubreport declarationSubreport;
    private HandlerRegistration closeFormDataHandlerRegistration;

    private final DispatchAsync dispatcher;
    private DeclarationDataPresenter declarationDataPresenter;

    private boolean selectRecord = false;
    private Map<String, Object> subreportParamValues;

    public interface MyView extends PopupView, HasUiHandlers<SubreportParamsUiHandlers> {
        void setSubreport(DeclarationSubreport declarationSubreport, Map<Long, RefBookParamInfo> refBookParamInfoMap, Date startDate, Date endDate);
        Map<String, Object> getFieldsValues() throws BadValueException, WarnValueException;
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
            subreportParamValues = getView().getFieldsValues();
            action.setSubreportParamValues(subreportParamValues);
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
            SaveLogEntriesAction action = createLogEntriesActionFromException(bve, LogLevel.ERROR);
            dispatcher.execute(action,
                    CallbackUtils.defaultCallback(new SaveLogEntriesCallBack(), this));
        } catch (WarnValueException wve) {
            Dialog.warningMessage("Отчет не сформирован", createDialogMessage(wve));
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
            action.setCreate(true);
            if (selectRecord) {
                action.setSelectedRow(getView().getSelectedRow());
                action.setSubreportParamValues(subreportParamValues);
            } else {
                action.setSubreportParamValues(getView().getFieldsValues());
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
            SaveLogEntriesAction action = createLogEntriesActionFromException(bve, LogLevel.ERROR);
            dispatcher.execute(action,
                    CallbackUtils.defaultCallback(new SaveLogEntriesCallBack(), this));
        } catch (WarnValueException wve) {
            Dialog.warningMessage("Отчет не сформирован", createDialogMessage(wve));
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

    private String createDialogMessage(AbstractBadValueException ex) {
        StringBuilder message = new StringBuilder();
        for (String entry : ex){
            message.append(entry).append('\n');
        }
        message.deleteCharAt(message.length() - 1);
        return message.toString();
    }

    private SaveLogEntriesAction createLogEntriesActionFromException (AbstractBadValueException exception, LogLevel logLevel) {
        List<LogEntry> logEntries = new ArrayList<LogEntry>();
        for (String entry : exception){
            logEntries.add(new LogEntry(logLevel, entry));
        }

        SaveLogEntriesAction action = new SaveLogEntriesAction();
        action.setLogEntries(logEntries);

        return action;
    }

    private class SaveLogEntriesCallBack extends AbstractCallback<SaveLogEntriesResult> {
        @Override
        public void onSuccess(SaveLogEntriesResult result) {
            LogAddEvent.fire(SubreportParamsPresenter.this, result.getUuid());
        }
    }
}
