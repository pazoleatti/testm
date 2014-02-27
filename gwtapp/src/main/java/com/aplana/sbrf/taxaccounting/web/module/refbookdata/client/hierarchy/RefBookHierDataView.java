package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookTreePickerView;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.PickerState;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;

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
    DateMaskBoxPicker relevanceDate;

    @UiField
    LinkButton
            addRow,
            deleteRow;

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
                if (getUiHandlers() != null) {
                    pickerState.setVersionDate(getRelevanceDate());
                    getUiHandlers().onRelevanceDateChanged();
                }
            }
        });

        refbookDataTree.addValueChangeHandler(new ValueChangeHandler<Set<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<Long>> event) {
                getUiHandlers().onSelectionChanged();
            }
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
    public Long getSelectedId() {
        if (refbookDataTree.getSelectedIds().size() > 0) {
            return refbookDataTree.getSelectedIds().iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public Date getRelevanceDate() {
        return relevanceDate.getValue();
    }

    @Override
    public void setReadOnlyMode(boolean readOnly) {
        addRow.setVisible(!readOnly);
        deleteRow.setVisible(!readOnly);
    }

    @Override
    public void setAttributeId(Long attrId) {
        pickerState.setRefBookAttrId(attrId);
    }

    @UiHandler("addRow")
    void addRowButtonClicked(ClickEvent event) {
        refbookDataTree.clearSelected(true);
        if (getUiHandlers() != null) {
            getUiHandlers().onAddRowClicked();
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

}
