package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ComparatorWithNull;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Форма "Исполнитель и подписанты"
 */
public class SignersView extends PopupViewWithUiHandlers<SignersUiHandlers> implements SignersPresenter.MyView {

    private static final int NAME_AND_POSITION_MAX_LENGTH = 100;

    public interface Binder extends UiBinder<PopupPanel, SignersView> {
    }

    @UiField
    TextBox name;

    @UiField
    TextBox phone;

    @UiField
    GenericDataGrid<FormDataSigner> signersTable;

    @UiField
    Button upSigner;

    @UiField
    Button downSigner;

    @UiField
    LinkButton addSigner;

    @UiField
    LinkButton removeSigner;

    @UiField
    Button saveButton;

    @UiField
    Button cancelButton;

    @UiField
    HorizontalPanel buttonPanel;

    @UiField
    VerticalPanel directionPanel;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    Label reportDepartmentName;

    private final PopupPanel widget;
    private List<FormDataSigner> signers;
    private List<FormDataSigner> clonedSigners;
    private FormDataPerformer performer;
    private boolean readOnlyMode;
    private final SingleSelectionModel<FormDataSigner> selectionModel = new SingleSelectionModel<FormDataSigner>();

    private ListDataProvider<FormDataSigner> dataProvider = new ListDataProvider<FormDataSigner>();
    private ColumnSortEvent.ListHandler<FormDataSigner> sortHandler = new ColumnSortEvent.ListHandler<FormDataSigner>(dataProvider.getList());
    private HandlerRegistration columnSortEventRegistration;

    @Inject
    public SignersView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setPerformer(FormDataPerformer performer) {
        this.performer = performer;

        if (performer != null) {
            name.setText(performer.getName());
            phone.setText(performer.getPhone());
            reportDepartmentName.setText(performer.getReportDepartmentName());
        } else {
            name.setText("");
            phone.setText("");
            reportDepartmentName.setText("");
        }
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
        departmentPicker.setAvalibleValues(departments, availableDepartments);
    }

    @Override
    public void setDepartment(Integer department) {
        departmentPicker.setValue(Arrays.asList(department));
    }

    @Override
    public void setReportDepartmentName(String department) {
        reportDepartmentName.setText(department);
    }

    @Override
    public void setSigners(List<FormDataSigner> signers) {
        this.signers = signers;

        if (clonedSigners == null) {
            clonedSigners = new ArrayList<FormDataSigner>();
        } else {
            clonedSigners.clear();
        }

        if (signers != null) {
            copySigners(signers, clonedSigners);
        }

        setSigners();
    }

    @Override
    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        name.setEnabled(!readOnlyMode);
        phone.setEnabled(!readOnlyMode);
        buttonPanel.setVisible(!readOnlyMode);
        directionPanel.setVisible(!readOnlyMode);
        saveButton.setVisible(!readOnlyMode);
        departmentPicker.setEnabled(!readOnlyMode);
        initTable(readOnlyMode);
    }

    private void setSigners() {
        signersTable.setRowData(clonedSigners);
        dataProvider.setList(clonedSigners);
        sortHandler.setList(dataProvider.getList());
        signersTable.redraw();
    }

    private void copySigners(List<FormDataSigner> from, List<FormDataSigner> to) {
        for (FormDataSigner signer : from) {
            if (!signer.getName().isEmpty() || !signer.getPosition().isEmpty()) {
                FormDataSigner clonedSigner = new FormDataSigner();
                clonedSigner.setId(signer.getId());
                clonedSigner.setName(signer.getName());
                clonedSigner.setPosition(signer.getPosition());
                clonedSigner.setOrd(signer.getOrd());
                to.add(clonedSigner);
            }
        }
    }

    @UiHandler("upSigner")
    public void onUpSigner(ClickEvent event) {
        FormDataSigner signer = selectionModel.getSelectedObject();
        int ind = clonedSigners.indexOf(signer);

        if (signer != null) {
            if (ind > 0) {
                FormDataSigner exchange = clonedSigners.get(ind - 1);
                clonedSigners.set(ind - 1, signer);
                signer.setOrd(ind + 1);     // переопределяем порядок в модельке
                clonedSigners.set(ind, exchange);
                exchange.setOrd(ind + 2);   // переопределяем порядок в модельке
                setSigners();
                selectionModel.setSelected(signer, true);
            }
        }
    }

    @UiHandler("downSigner")
    public void onDownSigner(ClickEvent event) {
        FormDataSigner signer = selectionModel.getSelectedObject();
        int ind = clonedSigners.indexOf(signer);

        if (signer != null) {
            if (ind < clonedSigners.size() - 1) {
                FormDataSigner exchange = clonedSigners.get(ind + 1);
                clonedSigners.set(ind + 1, signer);
                signer.setOrd(ind + 2);
                clonedSigners.set(ind, exchange);
                exchange.setOrd(ind + 1);
                setSigners();
                selectionModel.setSelected(signer, true);
            }
        }
    }

    @UiHandler("addSigner")
    public void onAddSigner(ClickEvent event) {
        FormDataSigner signer = new FormDataSigner();
        signer.setName("");
        signer.setPosition("");
        clonedSigners.add(signer);
        setSigners();
    }

    @UiHandler("removeSigner")
    public void onRemoveSigner(ClickEvent event) {
        FormDataSigner signer = selectionModel.getSelectedObject();
        if(clonedSigners.contains(signer)){
            clonedSigners.remove(clonedSigners.indexOf(signer));
            setSigners();
        }
    }

    @UiHandler("saveButton")
    public void onSave(ClickEvent event) {
        onSave();
    }

    private void onSave() {
        if (performer == null) {
            performer = new FormDataPerformer();
        }

        if (name.getText().isEmpty()) {
            Dialog.warningMessage("Необходимо ввести ФИО исполнителя");
            return;
        }

        if (phone.getText().isEmpty()) {
            Dialog.warningMessage("Необходимо ввести телефон исполнителя");
            return;
        }

        if (departmentPicker.getValue() == null || departmentPicker.getValue().isEmpty()) {
            Dialog.warningMessage("Укажите подразделение-исполнитель.");
            return;
        }

        performer.setName(name.getText());
        performer.setPhone(phone.getText());
        performer.setPrintDepartmentId(departmentPicker.getValue().get(0));
        performer.setReportDepartmentName(reportDepartmentName.getText());

        if (validateSigners()) {
            if (signers == null) {
                signers = new ArrayList<FormDataSigner>();
            } else {
                signers.clear();
            }
            copySigners(clonedSigners, signers);
        } else {
            return;
        }

        getUiHandlers().onSave(performer, signers);
    }

    private boolean validateSigners() {
        for (FormDataSigner signer : clonedSigners) {
            if (signer.getName().isEmpty() || signer.getPosition().isEmpty()) {
                Dialog.warningMessage("Необходимо заполнить поля ФИО и Должность всех подписантов.");
                return false;
            }
        }

        return true;
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        final SignersView t = this;
        if (!readOnlyMode && !isEqualClonedAndCurrentSignersAndReporter()) {
            Dialog.confirmMessage("Первоначальные данные изменились, хотите применить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    t.onSave();
                    Dialog.hideMessage();
                }

                @Override
                public void no() {
                    t.hide();
                    Dialog.hideMessage();
                }

                @Override
                public void close() {
                    t.hide();
                    Dialog.hideMessage();
                }
            });
        } else {
            hide();
        }
    }

    private void initTable(final boolean readOnlyMode) {
        signersTable.setSelectionModel(selectionModel);
        // Clean columns
        while (signersTable.getColumnCount() > 0) {
            signersTable.removeColumn(0);
        }

        TextColumn<FormDataSigner> idColumn = new TextColumn<FormDataSigner>() {
            @Override
            public String getValue(FormDataSigner object) {
                if(readOnlyMode){
                    return String.valueOf(object.getOrd());
                } else {
                    return "" + (clonedSigners.indexOf(object) + 1);
                }
            }
        };
        idColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        AbstractCell<String> nameCell;
        AbstractCell<String> positionCell;

        if (readOnlyMode) {
            nameCell = new TextCell();
            positionCell = new TextCell();
            if(!dataProvider.getDataDisplays().contains(signersTable)){
                dataProvider.addDataDisplay(signersTable);
                columnSortEventRegistration = signersTable.addColumnSortHandler(sortHandler);
            }
        } else {
            if (dataProvider.getDataDisplays().contains(signersTable)) {
                dataProvider.removeDataDisplay(signersTable);
                columnSortEventRegistration.removeHandler();
            }
            nameCell = new TextInputCell();
            positionCell = new TextInputCell();
        }

        Column<FormDataSigner, String> nameColumn = new Column<FormDataSigner, String>(nameCell) {
            @Override
            public String getValue(FormDataSigner object) {
                return object.getName();
            }
        };
        nameColumn.setFieldUpdater(new FieldUpdater<FormDataSigner, String>() {
            @Override
            public void update(int index, FormDataSigner signer, String value) {
                if (value.length() <= NAME_AND_POSITION_MAX_LENGTH) {
                    signer.setName(value);
                } else {
                    signer.setName(value.substring(0, NAME_AND_POSITION_MAX_LENGTH));
                    Dialog.warningMessage("Количество символов для ФИО подписанта " +
                            "превысило допустимое значение "+NAME_AND_POSITION_MAX_LENGTH+".");
                }
            }
        });

        Column<FormDataSigner, String> positionColumn = new Column<FormDataSigner, String>(positionCell) {
            @Override
            public String getValue(FormDataSigner object) {
                return object.getPosition();
            }
        };
        positionColumn.setFieldUpdater(new FieldUpdater<FormDataSigner, String>() {
            @Override
            public void update(int index, FormDataSigner signer, String value) {
                if (value.length() <= NAME_AND_POSITION_MAX_LENGTH) {
                    signer.setPosition(value);
                } else {
                    signer.setPosition(value.substring(0, NAME_AND_POSITION_MAX_LENGTH));
                    Dialog.warningMessage("Количество символов для должности подписанта" +
                            " превысило допустимое значение "+NAME_AND_POSITION_MAX_LENGTH+".");
                }
            }
        });

        signersTable.addColumn(idColumn, "№ пп");
        signersTable.setColumnWidth(idColumn, 60, Style.Unit.PX);
        signersTable.addResizableColumn(nameColumn, "ФИО подписанта");
        signersTable.addResizableColumn(positionColumn, "Должность");

        if(readOnlyMode){
            idColumn.setSortable(true);
            nameColumn.setSortable(true);
            positionColumn.setSortable(true);
        }

        // компораторы для сортировки на клиенте
        sortHandler.setComparator(idColumn, new ComparatorWithNull<FormDataSigner, Integer>() {
            @Override
            public int compare(FormDataSigner o1, FormDataSigner o2) {
                return compareWithNull(o1.getOrd(),o2.getOrd());
            }
        });
        sortHandler.setComparator(nameColumn, new ComparatorWithNull<FormDataSigner, String >() {
            @Override
            public int compare(FormDataSigner o1, FormDataSigner o2) {
                return compareWithNull(o1.getName(), o2.getName());
            }
        });
        sortHandler.setComparator(positionColumn, new ComparatorWithNull<FormDataSigner, String >() {
            @Override
            public int compare(FormDataSigner o1, FormDataSigner o2) {
                return compareWithNull(o1.getPosition(), o2.getPosition());
            }
        });
    }

    private boolean isEqualClonedAndCurrentSignersAndReporter() {
        if (performer != null && performer.getName() != null && performer.getPhone() != null &&
                (name.getText().compareTo(performer.getName()) != 0 ||
                        phone.getText().compareTo(performer.getPhone()) != 0)) {
            return false;
        }
        if (signers != null && clonedSigners != null && signers.size() == clonedSigners.size()) {
            for (int i = 0; i < signers.size(); i++) {
                if (signers.get(i).getName().compareTo(clonedSigners.get(i).getName()) != 0 ||
                        signers.get(i).getPosition().compareTo(clonedSigners.get(i).getPosition()) != 0) {
                    return false;
                }
            }
        } else if ((clonedSigners != null ? clonedSigners.size() : 0) != 0) {
            return false;
        }
        return true;
    }
}
