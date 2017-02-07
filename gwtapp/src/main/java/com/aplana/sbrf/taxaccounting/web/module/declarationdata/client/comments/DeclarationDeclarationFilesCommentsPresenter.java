package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CompositeCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Презентер попап модального окна "Файлы и комментарии",
 * данное окно вызывается с формы нф
 *
 * @author Lhaziev
 */
public class DeclarationDeclarationFilesCommentsPresenter extends PresenterWidget<DeclarationDeclarationFilesCommentsPresenter.MyView> implements DeclarationFilesCommentsUiHandlers {
    public interface MyView extends PopupView, HasUiHandlers<DeclarationFilesCommentsUiHandlers> {
        void setTableData(List<DataRow<Cell>> tableData);
        boolean isReadOnlyMode();
        void setReadOnlyMode(boolean readOnlyMode);
        void setNote(String note);
        void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler);
        void addFile(List<DataRow<Cell>> files);
        HandlerRegistration addEndLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addStartLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler);
        CheckBoxHeader getCheckBoxHeader();

        NumericColumn getDeclarationDataIdColumn();
        RefBookColumn getFileTypeColumn();
        StringColumn getUuidColumn();
        StringColumn getFileNameColumn();
        StringColumn getDateColumn();
        StringColumn getUserColumn();
        StringColumn getDepartmentColumn();
        StringColumn getNoteColumn();
    }

    private static final String ERROR_MSG = "Операция не выполнена";

    private final DispatchAsync dispatcher;
    private HandlerRegistration closeFormDataHandlerRegistration;
    private DeclarationData declarationData;

    @Inject
    public DeclarationDeclarationFilesCommentsPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    public void setFormData(DeclarationData declarationData) {
        this.declarationData = declarationData;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        GetDeclarationFilesCommentsAction action = new GetDeclarationFilesCommentsAction();
        action.setDeclarationData(declarationData);
        dispatcher.execute(action, getCallback(false));
    }

    CompositeCallback<GetDeclarationFilesCommentsResult> getCallback(final boolean exit) {
        return CallbackUtils
                .defaultCallback(new AbstractCallback<GetDeclarationFilesCommentsResult>() {
                    @Override
                    public void onSuccess(GetDeclarationFilesCommentsResult result) {
                        LogCleanEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this);
                        LogAddEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this, result.getUuid());
                        if (exit) {
                            getView().hide();
                        } else {
                            getView().setReadOnlyMode(result.isReadOnlyMode());
                            getView().setNote(result.getNote());
                            getView().setTableData(toDataRow(result.getFiles()));
                            if (closeFormDataHandlerRegistration != null)
                                closeFormDataHandlerRegistration.removeHandler();
                            if (!result.isReadOnlyMode()) {
                                closeFormDataHandlerRegistration = Window.addCloseHandler(new CloseHandler<Window>() {
                                    @Override
                                    public void onClose(CloseEvent<Window> event) {
                                        closeFormDataHandlerRegistration.removeHandler();
                                        unlockForm();
                                    }
                                });
                            }
                        }
                    }
                }, this);
    }

    @Override
    public void onHide() {
        super.onHide();
        if (closeFormDataHandlerRegistration != null)
            closeFormDataHandlerRegistration.removeHandler();
        unlockForm();
    }

    private void unlockForm() {
        if (!getView().isReadOnlyMode()) {
            UnlockDeclarationFilesCommentsAction action = new UnlockDeclarationFilesCommentsAction();
            action.setDeclarationId(declarationData.getId());
            dispatcher.execute(action, CallbackUtils.emptyCallback());
        }
    }

    @Override
    public void onSaveClicked(String note, final List<DataRow<Cell>> files, boolean exit) {
        SaveDeclarationFilesCommentsAction action = new SaveDeclarationFilesCommentsAction();
        List<DataRow<Cell>> declarationDataFiles = new ArrayList<DataRow<Cell>>();
        declarationDataFiles.addAll(files);
        action.setDeclarationData(declarationData);
        action.setNote(note);
        action.setFiles(toDeclarationDataFile(declarationDataFiles));
        dispatcher.execute(action, getCallback(exit));
    }


    @Override
    protected void onBind() {
        super.onBind();
        getView().addFileUploadValueChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                AddDeclarationFileAction action = new AddDeclarationFileAction();
                action.setUuid(event.getValue());
                action.setDeclarationData(declarationData);
                dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<AddDeclarationFileResult>() {
                            @Override
                            public void onSuccess(AddDeclarationFileResult result) {
                                getView().addFile(toDataRow(result.getFiles()));
                            }
                        }, DeclarationDeclarationFilesCommentsPresenter.this));
            }
        });
        getView().addEndLoadFileHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                if (event.isHasError()) {
                    Dialog.errorMessage("Ошибка", ERROR_MSG);
                }
                LogAddEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this, event.getUuid());
                LockInteractionEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this, false);
            }
        });
        getView().addStartLoadFileHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                LockInteractionEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this, true);
            }
        });
    }

    private DataRow<Cell> createDataRow() {
        DataRow<Cell> dataRow = new DataRow<Cell>();

        Cell declarationDataIdCell = new Cell();
        declarationDataIdCell.setColumn(getView().getDeclarationDataIdColumn());

        Cell uuidCell = new Cell();
        uuidCell.setColumn(getView().getUuidColumn());

        Cell fileTypeCell = new Cell();
        fileTypeCell.setColumn(getView().getFileTypeColumn());
        fileTypeCell.setEditable(true);

        Cell fileNameCell = new Cell();
        fileNameCell.setColumn(getView().getFileNameColumn());

        Cell dateCell = new Cell();
        dateCell.setColumn(getView().getDateColumn());

        Cell userNameCell = new Cell();
        userNameCell.setColumn(getView().getUserColumn());

        Cell userDepartmentNameCell = new Cell();
        userDepartmentNameCell.setColumn(getView().getDepartmentColumn());

        Cell noteCell = new Cell();
        noteCell.setColumn(getView().getNoteColumn());
        fileTypeCell.setEditable(true);

        dataRow.setFormColumns(Arrays.asList(
                declarationDataIdCell,
                fileTypeCell,
                uuidCell,
                fileNameCell,
                dateCell,
                userNameCell,
                userDepartmentNameCell,
                noteCell
        ));
        return dataRow;
    }

    private DataRow<Cell> toDataRow(DeclarationDataFile declarationDataFile) {
        DataRow<Cell> result = createDataRow();

        result.getCell(getView().getFileTypeColumn().getAlias()).setNumericValue(BigDecimal.valueOf(declarationDataFile.getFileTypeId()));
        result.getCell(getView().getFileTypeColumn().getAlias()).setRefBookDereference(declarationDataFile.getFileTypeName());
        result.getCell(getView().getDeclarationDataIdColumn().getAlias()).setNumericValue(new BigDecimal(declarationDataFile.getDeclarationDataId()));
        result.getCell(getView().getUuidColumn().getAlias()).setStringValue(declarationDataFile.getUuid());
        result.getCell(getView().getFileNameColumn().getAlias()).setStringValue(declarationDataFile.getFileName());
        result.getCell(getView().getDateColumn().getAlias()).setDateValue(declarationDataFile.getDate());
        result.getCell(getView().getUserColumn().getAlias()).setStringValue(declarationDataFile.getUserName());
        result.getCell(getView().getDepartmentColumn().getAlias()).setStringValue(declarationDataFile.getUserDepartmentName());
        result.getCell(getView().getNoteColumn().getAlias()).setStringValue(declarationDataFile.getNote());

        return result;
    }

    private List<DataRow<Cell>> toDataRow(List<DeclarationDataFile> declarationDataFileList) {
        List<DataRow<Cell>> result = new ArrayList<DataRow<Cell>>();

        for (DeclarationDataFile dataFile : declarationDataFileList) {
            result.add(toDataRow(dataFile));
        }

        return result;
    }

    private DeclarationDataFile toDeclarationDataFile(DataRow<Cell> dataRow) {
        DeclarationDataFile result = new DeclarationDataFile();

        result.setFileTypeId(dataRow.getCell(getView().getFileTypeColumn().getAlias()).getNumericValue().longValue());
        result.setFileTypeName(dataRow.getCell(getView().getFileTypeColumn().getAlias()).getRefBookDereference());
        result.setDeclarationDataId(dataRow.getCell(getView().getDeclarationDataIdColumn().getAlias()).getNumericValue().longValue());
        result.setUuid(dataRow.getCell(getView().getUuidColumn().getAlias()).getStringValue());
        result.setFileName(dataRow.getCell(getView().getFileNameColumn().getAlias()).getStringValue());
        result.setDate(dataRow.getCell(getView().getDateColumn().getAlias()).getDateValue());
        result.setUserName(dataRow.getCell(getView().getUserColumn().getAlias()).getStringValue());
        result.setUserDepartmentName(dataRow.getCell(getView().getDepartmentColumn().getAlias()).getStringValue());
        result.setNote(dataRow.getCell(getView().getNoteColumn().getAlias()).getStringValue());

        return result;
    }

    private List<DeclarationDataFile> toDeclarationDataFile(List<DataRow<Cell>> dataRowList) {
        List<DeclarationDataFile> result = new ArrayList<DeclarationDataFile>();

        for (DataRow<Cell> dataRow : dataRowList) {
            result.add(toDeclarationDataFile(dataRow));
        }

        return result;
    }

    public List<DataRow<Cell>> copyFiles(List<DataRow<Cell>> from) {
        List<DataRow<Cell>> to = new ArrayList<DataRow<Cell>>();
        for (DataRow<Cell> file : from) {
            DataRow<Cell> clonedFile = createDataRow();
            clonedFile.getCell(getView().getUuidColumn().getAlias()).setStringValue(file.getCell(getView().getUuidColumn().getAlias()).getStringValue());
            clonedFile.getCell(getView().getNoteColumn().getAlias()).setStringValue(file.getCell(getView().getNoteColumn().getAlias()).getStringValue());
            clonedFile.getCell(getView().getFileTypeColumn().getAlias()).setNumericValue(file.getCell(getView().getFileTypeColumn().getAlias()).getNumericValue());
            to.add(clonedFile);
        }
        return to;
    }
}

