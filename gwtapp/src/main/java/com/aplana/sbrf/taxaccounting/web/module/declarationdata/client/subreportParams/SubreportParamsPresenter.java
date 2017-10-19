package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
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
import com.google.gwt.i18n.client.DateTimeFormat;
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
    private Map<String, Object> subreportParamValues;

    public interface MyView extends PopupView, HasUiHandlers<SubreportParamsUiHandlers> {
        void setSubreport(DeclarationSubreport declarationSubreport, Map<Long, RefBookParamInfo> refBookParamInfoMap, Date startDate, Date endDate);

        Map<String, Object> getFieldsValues() throws BadValueException, WarnValueException;

        void setTableData(PrepareSpecificReportResult prepareSpecificReportResult);

        DataRow<Cell> getSelectedRow();

        void updateInfoLabel(boolean visible, String text, Map<String, String> styleMap);

        void addEnterNativePreviewHandler();

        void removeEnterNativePreviewHandler();

        Map<String, Object> getPersonFieldsValues() throws BadValueException, WarnValueException;
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
        getView().addEnterNativePreviewHandler();
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
            getView().updateInfoLabel(false, null, null);
            final PrepareSubreportAction action = new PrepareSubreportAction();
            action.setDeclarationDataId(declarationDataPresenter.getDeclarationId());
            action.setTaxType(declarationDataPresenter.getTaxType());
            action.setType(declarationSubreport.getAlias());
            if (declarationSubreport.getAlias().equals(SubreportAliasConstants.RNU_NDFL_PERSON_DB) || declarationSubreport.getAlias().equals(SubreportAliasConstants.REPORT_2NDFL)) {
                subreportParamValues = getView().getPersonFieldsValues();
            } else {
                subreportParamValues = getView().getFieldsValues();
            }
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
                                if (result.getPrepareSpecificReportResult().getDataRows().isEmpty()) {
                                    Map<String, String> warnMap = new LinkedHashMap<String, String>();
                                    StringBuilder message = new StringBuilder("Физическое лицо: ");
                                    for (Map.Entry<String, Object> entry : action.getSubreportParamValues().entrySet()) {
                                        if (entry.getValue() != null) {
                                            String token = "";
                                            if (entry.getValue() instanceof Date) {
                                                token = DateTimeFormat.getFormat("dd.MM.yyyy").format((Date) entry.getValue());
                                            } else {
                                                token = entry.getValue().toString();
                                            }
                                            message.append(token).append("; ");
                                        }
                                    }
                                    message.delete(message.length() - 2, message.length());
                                    message.append(" не найдено в форме");
                                    warnMap.put("", message.toString());
                                    try {
                                        throw new WarnValueException(warnMap);
                                    } catch (WarnValueException e) {
                                        Dialog.warningMessage("Отчет не сформирован", createDialogMessage(e));
                                    }
                                }
                                int countAvailbaleDataRows = result.getPrepareSpecificReportResult().getCountAvailableDataRows();
                                int resultSize = result.getPrepareSpecificReportResult().getDataRows().size();
                                if (resultSize < countAvailbaleDataRows) {
                                    StringBuilder infoMessage = new StringBuilder();
                                    Map<String, String> styleMap = new HashMap<String, String>();
                                    infoMessage.append("Найдено ")
                                            .append(countAvailbaleDataRows)
                                            .append(" записей. Отображено записей ")
                                            .append(resultSize)
                                            .append(". Уточните критерии поиска.");
                                    getView().updateInfoLabel(true, infoMessage.toString(), styleMap);
                                } else {
                                    StringBuilder infoMessage = new StringBuilder();
                                    Map<String, String> styleMap = new HashMap<String, String>();
                                    infoMessage.append("Найдено записей ")
                                            .append(countAvailbaleDataRows)
                                            .append(".");
                                    getView().updateInfoLabel(true, infoMessage.toString(), styleMap);
                                }
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
            Map<String, String> styleMap = new HashMap<String, String>();
            getView().updateInfoLabel(true, createDialogMessage(wve), styleMap);
            LogCleanEvent.fire(SubreportParamsPresenter.this);
            List<GetLogAction.PairLogLevelMessage> messagesList = new LinkedList<GetLogAction.PairLogLevelMessage>();
            Iterator<String> iterator = wve.iterator();
            while (iterator.hasNext()) {
                messagesList.add(new GetLogAction.PairLogLevelMessage(GetLogAction.LogLevel.WARN, iterator.next()));
            }
            GetLogAction action = new GetLogAction();
            action.setMessages(messagesList);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetLogResult>() {
                @Override
                public void onSuccess(GetLogResult result) {
                    LogAddEvent.fire(SubreportParamsPresenter.this, result.getUuid());
                }
            }, SubreportParamsPresenter.this));
        }
    }

    @Override
    public void onCreate() {
        try {
            getView().updateInfoLabel(false, null, null);
            ;
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
            Map<String, String> styleMap = new HashMap<String, String>();
            getView().updateInfoLabel(true, createDialogMessage(wve), styleMap);
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
        getView().updateInfoLabel(false, null, null);
        getView().removeEnterNativePreviewHandler();
        closeFormDataHandlerRegistration.removeHandler();
    }

    private String createDialogMessage(AbstractBadValueException ex) {
        StringBuilder message = new StringBuilder();
        for (String entry : ex) {
            message.append(entry).append('\n');
        }
        message.deleteCharAt(message.length() - 1);
        return message.toString();
    }

    private SaveLogEntriesAction createLogEntriesActionFromException(AbstractBadValueException exception, LogLevel logLevel) {
        List<GWTLogEntry> logEntries = new ArrayList<GWTLogEntry>();
        for (String entry : exception) {
            logEntries.add(new GWTLogEntry(logLevel, entry));
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