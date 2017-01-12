package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
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

import java.util.ArrayList;
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
    GenericDataGrid<DeclarationDataFile> table;
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

    private List<DeclarationDataFile> oldFiles = null;
    private String oldNote = null;

    private ListDataProvider<DeclarationDataFile> dataProvider = new ListDataProvider<DeclarationDataFile>();
    private MultiSelectionModel<DeclarationDataFile> selectionModel = new MultiSelectionModel<DeclarationDataFile>();
    private CheckBoxHeader checkBoxHeader = new CheckBoxHeader();
    private DefaultSelectionEventManager<DeclarationDataFile> multiSelectManager = createCustomManager(
            new DefaultSelectionEventManager.CheckboxEventTranslator<DeclarationDataFile>()
    );

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
                for (DeclarationDataFile row: table.getVisibleItems()) {
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
                for (DeclarationDataFile row : selectionModel.getSelectedSet()) {
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

        Column<DeclarationDataFile, Boolean> checkColumn = new Column<DeclarationDataFile, Boolean>(
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(DeclarationDataFile object) {
                return selectionModel.isSelected(object);
            }
        };

        Column<DeclarationDataFile, String> fileColumn = new Column<DeclarationDataFile, String>(new ClickableTextCell()){

            @Override
            public void render(Cell.Context context, DeclarationDataFile object, SafeHtmlBuilder sb) {
                String link = "<a href=\"" + GWT.getHostPageBaseURL() + "download/downloadBlobController/DeclarationDataFile/" + object.getUuid() + "\">" + object.getFileName() + "</a>";
                sb.appendHtmlConstant(link);
            }

            @Override
            public String getValue(DeclarationDataFile object) {
                return object.getFileName();
            }
        };

        AbstractCell<String> noteCell;
        if (readOnlyMode) {
            noteCell = new TextCell();
        } else {
            noteCell = new EditTextCell();
        }

        Column<DeclarationDataFile, String> noteColumn = new Column<DeclarationDataFile, String>(noteCell) {
            @Override
            public String getValue(DeclarationDataFile object) {
                return object.getNote()!=null?object.getNote():"";
            }
        };
        noteColumn.setFieldUpdater(new FieldUpdater<DeclarationDataFile, String>() {
            @Override
            public void update(int index, DeclarationDataFile object, String value) {
                if (value.length() <= NOTE_MAX_LENGTH) {
                    object.setNote(value);
                } else {
                    object.setNote(value.substring(0, NOTE_MAX_LENGTH));
                    Dialog.warningMessage("Количество символов для комментария превысило допустимое значение " + NOTE_MAX_LENGTH + ".");
                }
            }
        });

        TextColumn<DeclarationDataFile> dateColumn = new TextColumn<DeclarationDataFile>() {
            @Override
            public String getValue(DeclarationDataFile object) {
                return format.format(object.getDate());
            }
        };

        TextColumn<DeclarationDataFile> userColumn = new TextColumn<DeclarationDataFile>() {
            @Override
            public String getValue(DeclarationDataFile object) {
                return object.getUserName();
            }
        };

        TextColumn<DeclarationDataFile> departmentColumn = new TextColumn<DeclarationDataFile>() {
            @Override
            public String getValue(DeclarationDataFile object) {
                return object.getUserDepartmentName();
            }
        };

        if (!readOnlyMode) {
            table.addColumn(checkColumn, checkBoxHeader);
            table.setColumnWidth(checkColumn, 2, Style.Unit.EM);
        } else {
            //фиктивная колонка для правильной работы таблицы
            TextColumn<DeclarationDataFile> fixColumn = new TextColumn<DeclarationDataFile>() {
                @Override
                public String getValue(DeclarationDataFile object) {
                    return "";
                }
            };
            table.addColumn(fixColumn, "");
            table.setColumnWidth(fixColumn, 0, Style.Unit.EM);
        }
        table.addColumn(fileColumn, "Файл");
        table.setColumnWidth(fileColumn, 13, Style.Unit.EM);
        table.addColumn(noteColumn, "Комментарий");
        table.addColumn(dateColumn, "Дата-время");
        table.setColumnWidth(dateColumn, 8.5, Style.Unit.EM);
        table.addColumn(userColumn, "Пользователь");
        table.setColumnWidth(userColumn, 10, Style.Unit.EM);
        table.addColumn(departmentColumn, "Подразделение пользователя");
        table.setColumnWidth(departmentColumn, 12, Style.Unit.EM);
    }

    private boolean isModify() {
        if (!(note.getValue().equals(oldNote) || note.getValue().isEmpty() && oldNote == null)) {
            return true;
        }
        if (oldFiles != null && dataProvider.getList() != null && oldFiles.size() == dataProvider.getList().size()) {
            for(int i = 0; i < oldFiles.size(); i++) {
                if (!dataProvider.getList().get(i).getUuid().equals(oldFiles.get(i).getUuid()) ||
                        !((dataProvider.getList().get(i).getNote() == null || dataProvider.getList().get(i).getNote().isEmpty()) && oldFiles.get(i).getNote() == null
                                || dataProvider.getList().get(i).getNote().equals(oldFiles.get(i).getNote()))) {
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

    private List<DeclarationDataFile> copyFiles(List<DeclarationDataFile> from) {
        List<DeclarationDataFile> to = new ArrayList<DeclarationDataFile>();
        for (DeclarationDataFile file : from) {
            DeclarationDataFile clonedFile = new DeclarationDataFile();
            clonedFile.setUuid(file.getUuid());
            clonedFile.setNote(file.getNote());
            to.add(clonedFile);
        }
        return to;
    }

    @Override
    public void setTableData(List<DeclarationDataFile> result) {
        sort(result);
        oldFiles = copyFiles(result);
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
    public void addFile(List<DeclarationDataFile> files) {
        checkBoxHeader.setValue(false);
        dataProvider.getList().addAll(0, files);
        sort(dataProvider.getList());
        table.redraw();
    }

    private void sort(List<DeclarationDataFile> files) {
        Collections.sort(files, new Comparator<DeclarationDataFile>() {
            @Override
            public int compare(DeclarationDataFile o1, DeclarationDataFile o2) {
                int compareFileName = o1.getFileName().compareTo(o2.getFileName());
                if (compareFileName != 0)
                    return compareFileName;
                return -o1.getDate().compareTo(o2.getDate());
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
}
