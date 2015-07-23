package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;

public class RefBookDataView extends ViewWithUiHandlers<RefBookDataUiHandlers> implements RefBookDataPresenter.MyView {

    interface UrlTemplates extends SafeHtmlTemplates {
        @Template("Все значения записи {0} (дата актуальности {1})")
        SafeHtml getName(String versionCount, String actualDate);
    }
    private static final UrlTemplates urlTemplates = GWT.create(UrlTemplates.class);
    DateTimeFormat dtf = DateTimeFormat.getFormat("dd.MM.yyyy");

    interface Binder extends UiBinder<Widget, RefBookDataView> {
	}


	@UiField
	Panel contentPanel, mainPanel;
	@UiField
	Label titleDesc;
	@UiField
    DateMaskBoxPicker relevanceDate;
    @UiField
    LinkButton addRow, deleteRow, edit;
    @UiField
    LinkAnchor backAnchor;
    @UiField
    LinkButton backToRefBookAnchor;
    @UiField
    Button search;
    @UiField
    Button cancelEdit;
    @UiField
    HTML separator, separator1;
    @UiField
    Label editModeLabel;
    @UiField
    TextBox filterText;
    @UiField
    Label relevanceDateLabel;
    @UiField
    LinkButton sendQuery;

    private boolean isVersion;

	@Inject
	public RefBookDataView(final Binder uiBinder) {
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
		if (slot == RefBookDataPresenter.TYPE_editFormPresenter) {
			contentPanel.clear();
			if (content!=null){
				contentPanel.add(content);
			}
		} else if(slot == RefBookDataPresenter.TYPE_mainFormPresenter){
            mainPanel.clear();
            if (content!=null){
                mainPanel.add(content);
            }
        }
		else {
			super.setInSlot(slot, content);
		}
	}

    @Override
    public void setRefBookNameDesc(String verCount, Date relDate) {
        titleDesc.setText(urlTemplates.getName(verCount, dtf.format(relDate)).asString());
    }

    @Override
	public void setRefBookNameDesc(String desc) {
		titleDesc.setText(desc);
	}

	@Override
	public Date getRelevanceDate() {
		return relevanceDate.getValue();
	}

    @UiHandler("addRow")
	void addRowButtonClicked(ClickEvent event) {
        //selectionModel.clear();
		if (getUiHandlers() != null) {
			getUiHandlers().onAddRowClicked();
		}
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
                    getUiHandlers().setMode(FormMode.VIEW);
                }
            });
        } else {
            getUiHandlers().setMode(FormMode.VIEW);
        }
    }

    @UiHandler("search")
    void searchButtonClicked(ClickEvent event) {
        getUiHandlers().onSearchClick();
    }

    @UiHandler("edit")
    void editButtonClicked(ClickEvent event) {
        getUiHandlers().setMode(FormMode.EDIT);
    }

    @UiHandler("sendQuery")
    void sendQueryButtonClicked(ClickEvent event) {
        getUiHandlers().sendQuery();
    }

	@UiHandler("deleteRow")
	void deleteRowButtonClicked(ClickEvent event) {
        getUiHandlers().onDeleteRowClicked();
	}

    @UiHandler("backToRefBookAnchor")
    void onBackToRefBookAnchorClicked(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onBackToRefBookAnchorClicked();
        }
    }

    @UiHandler("backAnchor")
    void onPrintButtonClicked(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onBackClicked();
        }
    }

	private HasHorizontalAlignment.HorizontalAlignmentConstant convertAlignment(HorizontalAlignment alignment) {
		switch (alignment) {
			case ALIGN_LEFT:
				return HasHorizontalAlignment.ALIGN_LEFT;
			case ALIGN_CENTER:
				return HasHorizontalAlignment.ALIGN_CENTER;
			case ALIGN_RIGHT:
				return HasHorizontalAlignment.ALIGN_RIGHT;
			default:
				return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}

    @Override
    public void updateMode(FormMode mode) {
        switch (mode){
            case EDIT:
                addRow.setVisible(true);
                deleteRow.setVisible(true);
                separator.setVisible(true);
                edit.setVisible(false);
                search.setEnabled(true);
                filterText.setEnabled(true);
                relevanceDate.setEnabled(true);
                break;
            case READ:
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                separator.setVisible(false);
                edit.setVisible(false);
                search.setEnabled(true);
                filterText.setEnabled(true);
                relevanceDate.setEnabled(true);
                break;
            case VIEW:
                edit.setVisible(true);
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
                search.setEnabled(false);
                filterText.setEnabled(false);
                separator.setVisible(false);
                relevanceDate.setEnabled(false);
                break;
        }
        cancelEdit.setVisible(!isVersion&&mode==FormMode.EDIT);
    }

    @Override
    public void updateSendQuery(boolean isAvailable) {
        sendQuery.setVisible(isAvailable);
        edit.setVisible(!isAvailable && edit.isVisible());
    }

    @Override
    public void setVersionedFields(boolean isVisible) {
        relevanceDate.setVisible(isVisible);
        relevanceDateLabel.setVisible(isVisible);
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
        separator1.setVisible(!isVersion);
        cancelEdit.setVisible(!isVersion);
    }

    @Override
    public String getSearchPattern() {
        return filterText.getText();
    }

    @Override
    public void resetSearchInputBox() {
        filterText.setValue("");
    }
}
