package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
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

import java.util.Date;

import static com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode.EDIT;
import static com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode.VIEW;

/**
 * User: avanteev
 */
public class RefBookHierView extends ViewWithUiHandlers<RefBookHierUIHandlers> implements RefBookHierPresenter.MyView {

    @UiField
    Panel contentPanel;
    @UiField
    Panel mainPanel;
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
    HTML separator, separatorVersion;
    @UiField
    Button search;
    @UiField
    TextBox filterText;
    @UiField
    LinkButton backToRefBookAnchor;
    @UiField
    LinkAnchor backAnchor;

    private boolean isVersion;

    @Override
    public void clearFilterInputBox() {
        filterText.setValue("");
    }

    @Override
    public Date getRelevanceDate() {
        return relevanceDate.getValue();
    }

    @Override
    public void updateView(FormMode mode) {
        switch (mode){
            case EDIT:
                addRow.setVisible(true);
                deleteRow.setVisible(true);
                separator.setVisible(true);
                edit.setVisible(false);
                cancelEdit.setVisible(true);
                search.setEnabled(true);
                filterText.setEnabled(true);
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
                relevanceDate.setEnabled(true);
                break;
            case CREATE:
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                cancelEdit.setVisible(false);
                search.setEnabled(false);
                filterText.setEnabled(false);
                separator.setVisible(false);
                relevanceDate.setEnabled(false);
                break;
        }
    }

    @Override
    public String getSearchPattern() {
        return filterText.getText();
    }

    @Override
    public void setVersionedFields(boolean isVisible) {
        separatorVersion.setVisible(isVisible);
        relevanceDate.setVisible(isVisible);
        relevanceDateLabel.setVisible(isVisible);
    }


    interface RefBookHierViewUiBinder extends UiBinder<Widget, RefBookHierView> {

    }
    @Inject
    public RefBookHierView(RefBookHierViewUiBinder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        relevanceDate.setValue(new Date());

        relevanceDate.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                if(event.getValue()==null){
                    relevanceDate.setValue(new Date());
                } else {
                    if (getUiHandlers() != null) {
                        getUiHandlers().onRelevanceDateChanged();
                    }
                }
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
        if (slot == RefBookHierPresenter.TYPE_editFormPresenter) {
            contentPanel.clear();
            if (content != null) {
                contentPanel.add(content);
            }
        } else if(slot == RefBookHierPresenter.TYPE_mainFormPresenter){
            mainPanel.clear();
            if (content!=null){
                mainPanel.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

    @UiHandler("edit")
    void editButtonClicked(ClickEvent event) {
        getUiHandlers().setMode(EDIT);
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

    @UiHandler("deleteRow")
    void deleteRowButtonClicked(ClickEvent event) {
        /*if (getSelectedId() == null) {
            return;
        }*/
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

    @UiHandler("search")
    void searchButtonClicked(ClickEvent event) {
        /*deleteRow.setVisible(false);*/
        getUiHandlers().searchButtonClicked();
    }

    @UiHandler("addRow")
    void addRowButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onAddRowClicked();
        }
    }

    @UiHandler("backToRefBookAnchor")
    void onBackToRefBookAnchorClicked(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onBackToRefBookAnchorClicked();
        }
    }

    @Override
    public void setRefBookNameDesc(String desc) {
        titleDesc.setText(desc);
    }

    @Override
    public void setVersionView(boolean isVersion) {
        this.isVersion = isVersion;
        edit.setVisible(!isVersion);
        filterText.setEnabled(!isVersion);
        filterText.setVisible(!isVersion);
        backAnchor.setVisible(!isVersion);
        backToRefBookAnchor.setVisible(isVersion);
        backToRefBookAnchor.setText(titleDesc.getText());
        relevanceDate.setVisible(!isVersion);
        relevanceDateLabel.setVisible(!isVersion);
        separator.setVisible(!isVersion);
        search.setVisible(!isVersion);
        separatorVersion.setVisible(!isVersion);
        cancelEdit.setVisible(!isVersion);
    }

}