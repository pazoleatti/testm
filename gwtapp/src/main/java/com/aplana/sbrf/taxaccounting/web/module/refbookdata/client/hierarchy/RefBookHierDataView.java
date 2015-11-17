package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookTreePickerView;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

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

    private PickerState pickerState = new PickerState();

    @Inject
    @UiConstructor
    public RefBookHierDataView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        pickerState.setMultiSelect(false);

        refbookDataTree.addValueChangeHandler(new ValueChangeHandler<Set<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<Long>> event) {
                getUiHandlers().onSelectionChanged();
            }
        });

    }

    @Override
    public void load(){
        refbookDataTree.load(pickerState, false);
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
    public void setSelected(Long recordId) {
        pickerState.setSetIds(Arrays.asList(recordId));
        load();
    }

    @Override
    public void setSelection(RefBookTreeItem parentRefBookItem) {
        refbookDataTree.clearSelected(false);
        if (parentRefBookItem != null)
            refbookDataTree.setSelection(Arrays.asList(parentRefBookItem));
    }

    @Override
    public void clearSelected() {
        refbookDataTree.clearSelected(true);
    }

    @Override
    public Long getSelectedId() {
        if (!refbookDataTree.getSelectedIds().isEmpty()) {
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
    public void setAttributeId(Long attrId) {
        pickerState.setRefBookAttrId(attrId);
    }

    @Override
    public void setPickerState(Date relevanceDate, String searchPattern) {
        pickerState.setVersionDate(relevanceDate);
        pickerState.setSearchPattern(searchPattern);
    }

    public void searchButtonClicked() {
        if (getUiHandlers() != null) {
            if (pickerState.getSearchPattern()!= null && !pickerState.getSearchPattern().isEmpty()){
                pickerState.setNeedReload(false);
                refbookDataTree.checkCount(pickerState.getSearchPattern().trim(), new CheckValuesCountHandler() {
                    @Override
                    public void onGetValuesCount(Integer count) {
                        if (count != null && count < 100 && count>0) {
                            loadAndSelect();
                        } else if (count != null && count == 0){
                            pickerState.setSetIds(new ArrayList<Long>(0));
                            refbookDataTree.cleanValues();
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

    @Override
    public void updateMode(FormMode mode) {
        refbookDataTree.setEnabled(mode != FormMode.CREATE);
    }

    @Override
    public void clearFilterInputBox() {
        pickerState.setSearchPattern("");
        /*refbookDataTree.load(pickerState);*/
    }
}
