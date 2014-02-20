package com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataPerformer;
import com.aplana.sbrf.taxaccounting.model.FormDataSigner;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
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

    public interface Binder extends UiBinder<PopupPanel, SignersView> {
    }

    @UiField
    TextBox name;

    @UiField
    TextBox phone;

    @UiField
    DataGrid<FormDataSigner> signersTable;

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

    private final PopupPanel widget;
    private List<FormDataSigner> signers;
    private List<FormDataSigner> clonedSigners;
    private FormDataPerformer performer;
    private boolean readOnlyMode;
    private final SingleSelectionModel<FormDataSigner> selectionModel = new SingleSelectionModel<FormDataSigner>();
    private static final int NAME_AND_POSITION_MAX_LENGTH = 100;

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
        } else {
            name.setText("");
            phone.setText("");
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
        if (readOnlyMode) {
            cancelButton.setText("Закрыть");
        } else {
            cancelButton.setText("Отмена");
        }
        saveButton.setVisible(!readOnlyMode);
        departmentPicker.setEnabled(!readOnlyMode);
        initTable(readOnlyMode);
    }

    private void setSigners() {
        signersTable.setRowData(clonedSigners);
        signersTable.redraw();
    }

    private void copySigners(List<FormDataSigner> from, List<FormDataSigner> to) {
        for (FormDataSigner signer : from) {
            if (!signer.getName().isEmpty() || !signer.getPosition().isEmpty()) {
                FormDataSigner clonedSigner = new FormDataSigner();
                clonedSigner.setId(signer.getId());
                clonedSigner.setName(signer.getName());
                clonedSigner.setPosition(signer.getPosition());
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
                clonedSigners.set(ind, exchange);
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
                clonedSigners.set(ind, exchange);
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
        clonedSigners.remove(clonedSigners.indexOf(signer));
        setSigners();
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
            Dialog.warningMessage("Внимание!", "Необходимо ввести ФИО исполнителя");
            return;
        }

        if (phone.getText().isEmpty()) {
            Dialog.warningMessage("Внимание!", "Необходимо ввести телефон исполнителя");
            return;
        }

        if (departmentPicker.getValue() == null || departmentPicker.getValue().isEmpty()) {
            Dialog.warningMessage("Внимание!", "Не указано подразделение-исполнитель!");
            return;
        }

        performer.setName(name.getText());
        performer.setPhone(phone.getText());
        performer.setPrintDepartmentId(departmentPicker.getValue().get(0));

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
                Dialog.warningMessage("Внимание!", "Необходимо ввести ФИО подписанта и должность");
                return false;
            }
        }

        return true;
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        final SignersView t = this;
        if (!readOnlyMode && !isEqualClonedAndCurrentSignersAndReporter()) {
            Dialog.confirmMessage("Подтверждение", "Первоначальные данные изменились, хотите применить изменения?", new DialogHandler() {
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

    private void initTable(boolean readOnlyMode) {
        signersTable.setSelectionModel(selectionModel);
        // Clean columns
        while (signersTable.getColumnCount() > 0) {
            signersTable.removeColumn(0);
        }

        TextColumn<FormDataSigner> idColumn = new TextColumn<FormDataSigner>() {
            @Override
            public String getValue(FormDataSigner object) {
                return "" + (clonedSigners.indexOf(object) + 1);
            }
        };
        idColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        signersTable.addColumn(idColumn, "№ пп");
        signersTable.setColumnWidth(idColumn, 40, Style.Unit.PX);

        AbstractCell<String> nameCell;
        AbstractCell<String> positionCell;

        if (readOnlyMode) {
            nameCell = new TextCell();
            positionCell = new TextCell();
        } else {
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
                    Dialog.warningMessage("Внимание!", "Количество символов для ФИО подписанта превысило допустимое значение 100");
                }
            }
        });
        signersTable.addColumn(nameColumn, "ФИО подписанта");

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
                    Dialog.warningMessage("Внимание!", "Количество символов для должности подписанта превысило допустимое значение 100");
                }
            }
        });
        signersTable.addColumn(positionColumn, "Должность");
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
