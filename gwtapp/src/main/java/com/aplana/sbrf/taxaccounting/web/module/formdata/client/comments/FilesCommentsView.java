package com.aplana.sbrf.taxaccounting.web.module.formdata.client.comments;

import com.aplana.gwt.client.*;
import com.aplana.gwt.client.TextArea;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client.ConstIncomeHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client.TableWithCheckedColumn;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.model.FilesCommentsRow;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListUtils;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.EditTextColumn;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.cell.client.*;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.List;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

/**
 * Представление попапа модыльного окна "Файлы и комментарии",
 * данное окно вызывается с формы нф
 *
 * @author lhaziev
 */
public class FilesCommentsView extends PopupViewWithUiHandlers<FilesCommentsUiHandlers> implements FilesCommentsPresenter.MyView{

    public interface Binder extends UiBinder<PopupPanel, FilesCommentsView> {
    }

    private List<FormDataFile> tableData = null;
    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

    private final PopupPanel widget;

    interface UrlTemplates extends SafeHtmlTemplates {

        @Template("{0}{1}")
        SafeHtml getColValue(String main, String optional);
    }
    private static final UrlTemplates urlTemplates = GWT.create(UrlTemplates.class);

    @UiField
    ModalWindow modalWindow;
    @UiField
    GenericDataGrid<FormDataFile> table;
    @UiField
    TextArea note;
    @UiField
    HorizontalPanel buttonPanel;
    @UiField
    FileUploadWidget addFile;
    @UiField
    LinkButton removeFile;
    @UiField
    Button saveButton, cancelButton;

    private boolean readOnlyMode;

    private ListDataProvider<FormDataFile> dataProvider = new ListDataProvider<FormDataFile>();

    private MultiSelectionModel<FormDataFile> selectionModel = new MultiSelectionModel<FormDataFile>();

    private CheckBoxHeader checkBoxHeader = new CheckBoxHeader();
    private DefaultSelectionEventManager<FormDataFile> multiSelectManager = createCustomManager(
            new DefaultSelectionEventManager.CheckboxEventTranslator<FormDataFile>()
    );

    @Inject
    public FilesCommentsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);

        table.setSelectionModel(selectionModel, multiSelectManager);

        checkBoxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                for (FormDataFile row: table.getVisibleItems()) {
                    selectionModel.setSelected(row, event.getValue());
                }
            }
        });

        selectionModel.addSelectionChangeHandler(
                new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        onSelection();
                    }
                });

        initColumns();
        table.setRowCount(0);
        dataProvider.addDataDisplay(table);

        addFile.addStartLoadHandler(new StartLoadFileEvent.StartLoadFileHandler() {
            @Override
            public void onStartLoad(StartLoadFileEvent event) {
                getUiHandlers().onStartLoadFile();
            }
        });
    }

    @Override
    public void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler) {
        addFile.addValueChangeHandler(changeHandler);
    }

    private void onSelection() {
        int selectedItemCount = selectionModel.getSelectedSet().size();
        int visibleItemCount = table.getVisibleItemCount();

        if (selectedItemCount < visibleItemCount) {
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
    @Override
    public Widget asWidget() {
        return widget;
    }

    @UiHandler("removeFile")
    public void onRemoveFileClicked(ClickEvent event){
        for (FormDataFile row : selectionModel.getSelectedSet()) {
            dataProvider.getList().remove(row);
        }
        table.redraw();
    }

    @UiHandler("saveButton")
    public void onSaveClicked(ClickEvent event){
        table.flush();
        getUiHandlers().onSaveClicked(note.getValue(), dataProvider.getList());
    }

    @UiHandler("cancelButton")
    public void onCloseClicked(ClickEvent event){
        hide();
    }

    private void initColumns() {
        table.removeAllColumns();

        Column<FormDataFile, Boolean> checkColumn = new Column<FormDataFile, Boolean>(
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(FormDataFile object) {
                return selectionModel.isSelected(object);
            }
        };

        Column<FormDataFile, String> fileColumn = new Column<FormDataFile, String>(new ClickableTextCell()){

            @Override
            public void render(Cell.Context context, FormDataFile object, SafeHtmlBuilder sb) {
                String link = "<a href=\"/download/downloadBlobController/processArchiveDownload/" + object.getUuid() + "\">" + object.getFileName() + "</a>";
                sb.appendHtmlConstant(link);
            }

            @Override
            public String getValue(FormDataFile object) {
                return object.getFileName();
            }
        };

        AbstractCell<String> noteCell;
        if (readOnlyMode) {
            noteCell = new TextCell();
        } else {
            noteCell = new EditTextCell();
        }

        Column<FormDataFile, String> noteColumn = new Column<FormDataFile, String>(noteCell) {
            @Override
            public String getValue(FormDataFile object) {
                return object.getNote()!=null?object.getNote():"";
            }
        };
        noteColumn.setFieldUpdater(new FieldUpdater<FormDataFile, String>() {
            @Override
            public void update(int index, FormDataFile object, String value) {
                object.setNote(value);
            }
        });

        TextColumn<FormDataFile> dateColumn = new TextColumn<FormDataFile>() {
            @Override
            public String getValue(FormDataFile object) {
                return format.format(object.getDate());
            }
        };

        TextColumn<FormDataFile> userColumn = new TextColumn<FormDataFile>() {
            @Override
            public String getValue(FormDataFile object) {
                return object.getUserName();
            }
        };

        TextColumn<FormDataFile> departmentColumn = new TextColumn<FormDataFile>() {
            @Override
            public String getValue(FormDataFile object) {
                return object.getUserDepartmentName();
            }
        };

        if (!readOnlyMode) {
            table.addColumn(checkColumn, checkBoxHeader);
            table.setColumnWidth(checkColumn, 2, Style.Unit.EM);
        } else {
            //фиктивная колонка для правильной работы таблицы
            TextColumn<FormDataFile> fixColumn = new TextColumn<FormDataFile>() {
                @Override
                public String getValue(FormDataFile object) {
                    return "";
                }
            };
            table.addColumn(fixColumn, "");
            table.setColumnWidth(fixColumn, 0, Style.Unit.EM);
        }
        table.addColumn(fileColumn, "Файл");
        table.addColumn(noteColumn, "Комментарий");
        table.setColumnWidth(noteColumn, 15, Style.Unit.EM);
        table.addColumn(dateColumn, "Дата-время");
        table.setColumnWidth(dateColumn, 8.5, Style.Unit.EM);
        table.addColumn(userColumn, "Пользователь");
        table.setColumnWidth(userColumn, 10, Style.Unit.EM);
        table.addColumn(departmentColumn, "Подразделение пользователя");
        table.setColumnWidth(departmentColumn, 10, Style.Unit.EM);
    }

    @Override
    public void setTableData(List<FormDataFile> result) {
        tableData = result;
        table.setRowData(tableData);
        dataProvider.setList(result);
        //table.setVisibleRange(new Range(0, result.size()));
        //table.flush();
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
        } else {
            buttonPanel.setVisible(true);
            saveButton.setVisible(true);
        }
        initColumns();
        table.redraw();

    }

    @Override
    public void setNote(String note) {
        this.note.setValue(note);
    }

    @Override
    public void addFile(FormDataFile file) {
        tableData.add(0, file);
        table.setRowData(tableData);
        dataProvider.setList(tableData);
        //table.setVisibleRange(new Range(0, dataProvider.getList().size()));
        //table.flush();
        table.redraw();
    }

    @Override
    public HandlerRegistration addEndLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler) {
        return addFile.addEndLoadHandler(handler);
    }
}
