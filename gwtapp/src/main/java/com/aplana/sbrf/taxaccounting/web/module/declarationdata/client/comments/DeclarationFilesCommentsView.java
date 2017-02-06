package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

/**
 * Представление попапа модального окна "Файлы и комментарии",
 * данное окно вызывается с формы нф
 *
 * @author Lhaziev
 */
public class DeclarationFilesCommentsView extends PopupViewWithUiHandlers<DeclarationFilesCommentsUiHandlers> implements DeclarationDeclarationFilesCommentsPresenter.MyView{

    public interface Binder extends UiBinder<PopupPanel, DeclarationFilesCommentsView> {
    }

    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
    private static final int NOTE_MAX_LENGTH = 255;

    private final PopupPanel widget;

    @UiField
    ModalWindow modalWindow;
    @UiField
    GenericDataGrid<DataRow<Cell>> table;
    @UiField
    com.google.gwt.user.client.ui.TextArea note;
    @UiField
    HorizontalPanel buttonPanel;
    @UiField
    FileUploadWidget addFile;
    @UiField
    LinkButton removeFile;
    @UiField
    Button saveButton, cancelButton;

    private boolean readOnlyMode;

    private List<DataRow<Cell>> oldFiles = null;
    private String oldNote = null;

    private ListDataProvider<DataRow<Cell>> dataProvider = new ListDataProvider<DataRow<Cell>>();
    private MultiSelectionModel<DataRow<Cell>> selectionModel = new MultiSelectionModel<DataRow<Cell>>();
    private CheckBoxHeader checkBoxHeader = new CheckBoxHeader();
    private DefaultSelectionEventManager<DataRow<Cell>> multiSelectManager = createCustomManager(
            new DefaultSelectionEventManager.CheckboxEventTranslator<DataRow<Cell>>()
    );

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    private NumericColumn declarationDataIdColumn = new NumericColumn();
    private StringColumn uuidColumn = new StringColumn();
    private StringColumn fileNameColumn = new StringColumn();
    private RefBookColumn fileTypeColumn = new RefBookColumn();
    private StringColumn noteColumn = new StringColumn();
    private StringColumn dateColumn = new StringColumn();
    private StringColumn userColumn = new StringColumn();
    private StringColumn departmentColumn = new StringColumn();

    @Inject
    public DeclarationFilesCommentsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);

        note.getElement().setAttribute("maxLength", "255");

        selectionModel.addSelectionChangeHandler(
                new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        onSelection();
                    }
                });
        table.setSelectionModel(selectionModel, multiSelectManager);
        checkBoxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                for (DataRow<Cell> row: table.getVisibleItems()) {
                    selectionModel.setSelected(row, event.getValue());
                }
            }
        });
        initColumns();
        table.setRowCount(0);
        dataProvider.addDataDisplay(table);
    }

    @UiHandler("removeFile")
    public void onRemoveFileClicked(ClickEvent event){
        Dialog.confirmMessage("Подтверждение удаления файлов", "Вы уверены, что хотите удалить выбранные файлы?", new DialogHandler() {
            @Override
            public void yes() {
                for (DataRow<Cell> row : selectionModel.getSelectedSet()) {
                    dataProvider.getList().remove(row);
                }
                table.redraw();
                super.yes();
            }
        });

    }

    @UiHandler("saveButton")
    public void onSaveClicked(ClickEvent event){
        table.flush();
        getUiHandlers().onSaveClicked(note.getValue(), dataProvider.getList(), false);
    }

    @UiHandler("cancelButton")
    public void onCloseClicked(ClickEvent event){
        if (!readOnlyMode && isModify() ) {
            Dialog.confirmMessage("Подтверждение", "Первоначальные данные изменились, применить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    getUiHandlers().onSaveClicked(note.getValue(), dataProvider.getList(), true);
                }

                @Override
                public void no() {
                    hide();
                }

                @Override
                public void cancel() {
                    super.cancel();
                }
            });
        } else {
            hide();
        }
    }

    private void onSelection() {
        int selectedItemCount = selectionModel.getSelectedSet().size();
        int visibleItemCount = table.getVisibleItemCount();

        if (selectedItemCount < visibleItemCount || selectedItemCount == 0) {
            checkBoxHeader.setValue(false);
        } else if (selectedItemCount == visibleItemCount) {
            checkBoxHeader.setValue(true);
        }

        if (selectedItemCount > 0) {
            removeFile.setEnabled(true);
        } else {
            removeFile.setEnabled(false);
        }
    }

    private void initColumns() {
        table.removeAllColumns();
        factory.setReadOnly(isReadOnlyMode());

        Column<DataRow<Cell>, Boolean> checkColumn = new Column<DataRow<Cell>, Boolean> (
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(DataRow<Cell> object) {
                return selectionModel.isSelected(object);
            }
        };

        uuidColumn.setAlias("uuid");
        uuidColumn.setName("uuid");

        declarationDataIdColumn.setAlias("declarationDataId");
        declarationDataIdColumn.setName("declarationDataId");

        fileTypeColumn.setAlias("fileType");
        fileTypeColumn.setName("fileType");
        fileTypeColumn.setId(1);
        fileTypeColumn.setNameAttributeId(9342L);
        fileTypeColumn.setRefBookAttributeId(9342L);
        fileTypeColumn.setRefBookAttribute(new RefBookAttribute(){{setAttributeType(RefBookAttributeType.STRING);}});
        fileTypeColumn.setSearchEnabled(false);
        fileTypeColumn.setVersioned(false);
        Column<DataRow<Cell>, ?> fileTypeColumnUI = factory.createTableColumn(fileTypeColumn, table);
        ((DataRowColumn<?>) fileTypeColumnUI).addCellModifiedEventHandler(new CellModifiedEventHandler() {
            @Override
            public void onCellModified(CellModifiedEvent event, boolean withReference) {
                if (getUiHandlers() != null) {
                    table.redraw();
                }
            }
        });

        fileNameColumn.setAlias("fileName");
        fileNameColumn.setName("Имя файла");
        DataRowColumn<String> fileNameColumnUI = new DataRowColumn<String>(new ClickableTextCell(), fileNameColumn) {
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context, DataRow<Cell> object, SafeHtmlBuilder sb) {
                String link = "<a href=\"" + GWT.getHostPageBaseURL() + "download/downloadBlobController/DeclarationDataFile/" + object.getCell(uuidColumn.getAlias()).getStringValue() + "\">" + object.getCell(fileNameColumn.getAlias()).getStringValue() + "</a>";
                sb.appendHtmlConstant(link);
            }

            @Override
            public String getValue(DataRow<Cell> object) {
                return object.getCell(fileTypeColumn.getAlias()).getStringValue();
            }
        };

        AbstractCell<String> noteCell;
        if (readOnlyMode) {
            noteCell = new TextCell();
        } else {
            noteCell = new EditTextCell();
        }

        noteColumn.setAlias("note");
        noteColumn.setName("Комментарий");
        DataRowColumn<String> noteColumnUI = new DataRowColumn<String>(noteCell, fileNameColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                String note = object.getCell(noteColumn.getAlias()).getStringValue();
                return note != null ? note : "";
            }
        };
        noteColumnUI.setFieldUpdater(new FieldUpdater<DataRow<Cell>, String>() {
            @Override
            public void update(int index, DataRow<Cell> object, String value) {
                if (value.length() <= NOTE_MAX_LENGTH) {
                    object.getCell(noteColumn.getAlias()).setStringValue(value);
                } else {
                    object.getCell(noteColumn.getAlias()).setStringValue(value.substring(0, NOTE_MAX_LENGTH));
                    Dialog.warningMessage("Количество символов для комментария превысило допустимое значение " + NOTE_MAX_LENGTH + ".");
                }
            }
        });

        dateColumn.setAlias("date");
        dateColumn.setName("Дата-время");
        DataRowColumn<String> dateColumnUI = new DataRowColumn<String>(new TextCell(), fileNameColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return format.format(object.getCell(dateColumn.getAlias()).getDateValue());
            }
        };

        userColumn.setAlias("userName");
        userColumn.setName("Пользователь");
        DataRowColumn<String> userColumnUI = new DataRowColumn<String>(new TextCell(), fileNameColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return object.getCell(userColumn.getAlias()).getStringValue();
            }
        };

        departmentColumn.setAlias("userDepartmentName");
        departmentColumn.setName("Подразделение пользователя");
        DataRowColumn<String> departmentColumnUI = new DataRowColumn<String>(new TextCell(), fileNameColumn) {
            @Override
            public String getValue(DataRow<Cell> object) {
                return object.getCell(departmentColumn.getAlias()).getStringValue();
            }
        };

        if (!readOnlyMode) {
            table.addColumn(checkColumn, checkBoxHeader);
            table.setColumnWidth(checkColumn, 2, Style.Unit.EM);
        } else {
            //фиктивная колонка для правильной работы таблицы
            TextColumn<DataRow<Cell>> fixColumn = new TextColumn<DataRow<Cell>>() {
                @Override
                public String getValue(DataRow<Cell> object) {
                    return "";
                }
            };
            table.addColumn(fixColumn, "");
            table.setColumnWidth(fixColumn, 0, Style.Unit.EM);
        }
        table.addColumn(fileNameColumnUI, fileNameColumn.getName());
        table.setColumnWidth(fileNameColumnUI, 13, Style.Unit.EM);

        table.addColumn(fileTypeColumnUI, fileTypeColumn.getName());
        table.setColumnWidth(fileNameColumnUI, 10, Style.Unit.EM);

        table.addColumn(noteColumnUI, noteColumn.getName());

        table.addColumn(dateColumnUI, dateColumn.getName());
        table.setColumnWidth(dateColumnUI, 8.5, Style.Unit.EM);

        table.addColumn(userColumnUI, userColumn.getName());
        table.setColumnWidth(userColumnUI, 10, Style.Unit.EM);

        table.addColumn(departmentColumnUI, departmentColumn.getName());
        table.setColumnWidth(departmentColumnUI, 12, Style.Unit.EM);
    }

    private boolean isModify() {
        if (!(note.getValue().equals(oldNote) || note.getValue().isEmpty() && oldNote == null)) {
            return true;
        }
        if (oldFiles != null && dataProvider.getList() != null && oldFiles.size() == dataProvider.getList().size()) {
            for(int i = 0; i < oldFiles.size(); i++) {
                DataRow<Cell> row = dataProvider.getList().get(i);
                String uuid = row.getCell(uuidColumn.getAlias()).getStringValue();
                String note = row.getCell(noteColumn.getAlias()).getStringValue();

                DataRow<Cell> oldRow = oldFiles.get(i);
                String oldUuid = oldRow.getCell(uuidColumn.getAlias()).getStringValue();
                String oldNote = oldRow.getCell(noteColumn.getAlias()).getStringValue();

                if (!uuid.equals(oldUuid)
                    || !((note == null || note.isEmpty()) && oldNote == null || (note != null && note.equals(oldNote)))
                ) {
                    return true;
                }
            }
        } else if ((dataProvider.getList() != null ? dataProvider.getList().size() : 0) != 0) {
            return true;
        }

        return false;
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler) {
        addFile.addValueChangeHandler(changeHandler);
    }

    @Override
    public void setTableData(List<DataRow<Cell>> result) {
        sort(result);
        oldFiles = getUiHandlers().copyFiles(result);
        dataProvider.setList(result);
        selectionModel.clear();
        onSelection();
    }

    @Override
    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    @Override
    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        if (readOnlyMode) {
            buttonPanel.setVisible(false);
            saveButton.setVisible(false);
            note.getElement().setAttribute("readonly", "readonly");
        } else {
            buttonPanel.setVisible(true);
            saveButton.setVisible(true);
            note.getElement().removeAttribute("readonly");
        }
        initColumns();
        table.redraw();
    }

    @Override
    public void setNote(String note) {
        oldNote = note;
        this.note.setValue(note);
    }

    @Override
    public void addFile(List<DataRow<Cell>> files) {
        checkBoxHeader.setValue(false);
        dataProvider.getList().addAll(0, files);
        sort(dataProvider.getList());
        table.redraw();
    }

    private void sort(List<DataRow<Cell>> files) {
        Collections.sort(files, new Comparator<DataRow<Cell>>() {
            @Override
            public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
                int compareFileName = o1.getCell(fileNameColumn.getAlias()).getStringValue().compareTo(o2.getCell(fileNameColumn.getAlias()).getStringValue());

                if (compareFileName != 0) {
                    return compareFileName;
                }

                return -o1.getCell(dateColumn.getAlias()).getDateValue().compareTo(o2.getCell(dateColumn.getAlias()).getDateValue());
            }
        });
    }
    @Override
    public HandlerRegistration addEndLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler) {
        return addFile.addEndLoadHandler(handler);
    }

    @Override
    public HandlerRegistration addStartLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler) {
        return addFile.addEndLoadHandler(handler);
    }

    public CheckBoxHeader getCheckBoxHeader() {
        return checkBoxHeader;
    }

    public StringColumn getFileNameColumn() {
        return fileNameColumn;
    }

    public StringColumn getDateColumn() {
        return dateColumn;
    }

    public StringColumn getUserColumn() {
        return userColumn;
    }

    public StringColumn getDepartmentColumn() {
        return departmentColumn;
    }

    public StringColumn getNoteColumn() {
        return noteColumn;
    }

    public StringColumn getUuidColumn() {
        return uuidColumn;
    }

    public NumericColumn getDeclarationDataIdColumn() {
        return declarationDataIdColumn;
    }

    public RefBookColumn getFileTypeColumn() {
        return fileTypeColumn;
    }
}
