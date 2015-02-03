package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookTreePickerView;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode.*;

/**
 * Представление формы редактирования иерархического справочника
 *
 * @author aivanov
 */
public class RefBookHierDataView extends ViewWithUiHandlers<RefBookHierDataUiHandlers> implements RefBookHierDataPresenter.MyView {

    interface Binder extends UiBinder<Widget, RefBookHierDataView> {
    }

    @UiField
    RefBookTreePickerView refbookDataTree;

    @UiField
    Panel contentPanel;
    @UiField
    Label titleDesc;
    @UiField
    Label relevanceDateLabel;
    @UiField
    DateMaskBoxPicker relevanceDate;

    @UiField
    LinkButton
            addRow,
            deleteRow;

    @UiField
    LinkButton edit;
    @UiField
    Button cancelEdit;
    @UiField
    HTML separator;
    @UiField
    HTML separatorVersion;
    @UiField
    Button search;
    @UiField
    TextBox filterText;

    private PickerState pickerState = new PickerState();

    @Inject
    public RefBookHierDataView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        relevanceDate.setValue(new Date());

        pickerState.setVersionDate(relevanceDate.getValue());
        pickerState.setMultiSelect(false);

        relevanceDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                if(event.getValue()==null){
                    relevanceDate.setValue(new Date());
                } else {
                    if (getUiHandlers() != null) {
                        pickerState.setVersionDate(getRelevanceDate());
                        getUiHandlers().onRelevanceDateChanged();
                    }
                }
            }
        });

        refbookDataTree.addValueChangeHandler(new ValueChangeHandler<Set<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<Long>> event) {
                getUiHandlers().onSelectionChanged();
            }
        });

        filterText.addKeyPressHandler(new HandlesAllKeyEvents() {
            @Override
            public void onKeyDown(KeyDownEvent event) {}

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getUnicodeCharCode() == KeyCodes.KEY_ENTER){
                    search.click();
                }
            }

            @Override
            public void onKeyUp(KeyUpEvent event) {}
        });

    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == RefBookHierDataPresenter.TYPE_editFormPresenter) {
            contentPanel.clear();
            if (content != null) {
                contentPanel.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void load(){
        refbookDataTree.load(pickerState);
    }

    @Override
    public void loadAndSelect(){
        refbookDataTree.selectFirstItenOnLoad();
        load();
    }

    @Override
    public void reload() {
        refbookDataTree.reload();
    }

    @Override
    public void setRefBookNameDesc(String desc) {
        titleDesc.setText(desc);
    }

    @Override
    public void setSelected(Long recordId) {
        pickerState.setSetIds(Arrays.asList(recordId));
        load();
    }

    @Override
    public void setSelection(RefBookTreeItem parentRefBookItem) {
        if (parentRefBookItem != null)
            refbookDataTree.setSelection(Arrays.asList(parentRefBookItem));
    }

    @Override
    public void clearSelected() {
        refbookDataTree.clearSelected(true);
    }

    @Override
    public Long getSelectedId() {
        if (refbookDataTree.getSelectedIds().size() > 0) {
            return refbookDataTree.getSelectedIds().iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public RefBookTreeItem getSelectedItem() {
        List<RefBookTreeItem> refBookTreeItems = refbookDataTree.getSelectionValues();
        if (refBookTreeItems.size() == 1) {
            return refBookTreeItems.get(0);
        }
        return null;
    }

    @Override
    public RefBookTreeItem getItemById(Long recordId) {
        return refbookDataTree.getUiTreeItem(recordId).getRefBookTreeItem();
    }

    @Override
    public void deleteItem(Long id){
        refbookDataTree.deleteRecord(id);
    }

    @Override
    public void updateItem(Long id, Long newParentId, String newName){
        refbookDataTree.updateRecord(id, newParentId, newName);
    }

    @Override
    public Date getRelevanceDate() {
        return relevanceDate.getValue();
    }

    @Override
    public void setAttributeId(Long attrId) {
        pickerState.setRefBookAttrId(attrId);
    }

    @UiHandler("addRow")
    void addRowButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onAddRowClicked();
        }
    }

    @UiHandler("search")
    void searchButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            pickerState.setSearchPattern(filterText.getValue());
            if (filterText.getValue()!= null && !filterText.getValue().isEmpty()){
                pickerState.setNeedReload(false);
                refbookDataTree.checkCount(filterText.getValue().trim(), new CheckValuesCountHandler() {
                    @Override
                    public void onGetValuesCount(Integer count) {
                        if (count != null && count < 100 && count>0) {
                            refbookDataTree.selectFirstItenOnLoad();
                            loadAndSelect();
                        } else if (count != null && count == 0){
                            pickerState.setSetIds(new ArrayList<Long>(0));
                            refbookDataTree.cleanValues();
                            deleteRow.setVisible(false);
                            getUiHandlers().onCleanEditForm();
                        } else {
                            Dialog.warningMessage("Уточните параметры поиска: найдено слишком много значений.");
                        }
                    }
                });
            } else {
                pickerState.setNeedReload(true);
                loadAndSelect();
            }

            getUiHandlers().onCleanEditForm();
        }
    }

    @UiHandler("deleteRow")
    void deleteRowButtonClicked(ClickEvent event) {
        if (getSelectedId() == null) {
            return;
        }
        Dialog.confirmMessage("Подтверждение", "Удалить выбранную запись справочника?",
                new DialogHandler() {
                    @Override
                    public void yes() {
                        if (getUiHandlers() != null) {
                            getUiHandlers().onDeleteRowClicked();
                        }
                        Dialog.hideMessage();
                    }

                    @Override
                    public void no() {
                        Dialog.hideMessage();
                    }

                    @Override
                    public void close() {
                        no();
                    }
                });
    }

    @UiHandler("cancelEdit")
    void cancelEditButtonClicked(ClickEvent event) {
        if (getUiHandlers().isFormModified()) {
            Dialog.confirmMessage("Подтверждение изменений", "Строка была изменена. Сохранить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    getUiHandlers().saveChanges();
                }

                @Override
                public void no() {
                    getUiHandlers().cancelChanges();
                    getUiHandlers().setMode(VIEW);
                }
            });
        } else {
            getUiHandlers().setMode(VIEW);
        }
    }

    @UiHandler("edit")
    void editButtonClicked(ClickEvent event) {
        getUiHandlers().setMode(EDIT);
    }

    @Override
    public void updateMode(FormMode mode) {
        switch (mode){
            case EDIT:
                addRow.setVisible(true);
                deleteRow.setVisible(true);
                separator.setVisible(true);
                edit.setVisible(false);
                cancelEdit.setVisible(true);
                search.setEnabled(true);
                filterText.setEnabled(true);
                refbookDataTree.setEnabled(true);
                relevanceDate.setEnabled(true);
                break;
            case READ:
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                separator.setVisible(false);
                edit.setVisible(false);
                cancelEdit.setVisible(false);
                search.setEnabled(true);
                filterText.setEnabled(true);
                refbookDataTree.setEnabled(true);
                relevanceDate.setEnabled(true);
                break;
            case VIEW:
                edit.setVisible(true);
                cancelEdit.setVisible(false);
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                separator.setVisible(false);
                search.setEnabled(true);
                filterText.setEnabled(true);
                refbookDataTree.setEnabled(true);
                relevanceDate.setEnabled(true);
                break;
            case CREATE:
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                cancelEdit.setVisible(false);
                search.setEnabled(false);
                filterText.setEnabled(false);
                separator.setVisible(false);
                refbookDataTree.setEnabled(false);
                relevanceDate.setEnabled(false);
                break;
        }
    }

    @Override
    public void clearFilterInputBox() {
        if (!filterText.getText().equals("")) {
            pickerState.setSearchPattern("");
            filterText.setValue("");
            refbookDataTree.load(pickerState);
        }
    }

    @Override
    public void setVersionedFields(boolean isVisible) {
        separatorVersion.setVisible(isVisible);
        relevanceDate.setVisible(isVisible);
        relevanceDateLabel.setVisible(isVisible);
    }
}
