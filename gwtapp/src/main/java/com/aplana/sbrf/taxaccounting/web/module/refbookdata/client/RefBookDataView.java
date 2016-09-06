package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.DropdownButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.dom.client.Style;
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
import java.util.List;

public class RefBookDataView extends ViewWithUiHandlers<RefBookDataUiHandlers> implements RefBookDataPresenter.MyView {

    interface Binder extends UiBinder<Widget, RefBookDataView> {
	}


	@UiField
	Panel contentPanel, mainPanel;
	@UiField
	Label titleDesc, editModeLabel, relevanceDateLabel;
	@UiField
    DateMaskBoxPicker relevanceDate;
    @UiField
    LinkButton addRow, deleteRow, edit, backToRefBookAnchor;
    @UiField
    LinkAnchor backAnchor;
    @UiField
    Button search, cancelEdit;
    @UiField
    CheckBox exactSearch;
    @UiField
    HTML separator, separator1;
    @UiField
    TextBox filterText;
    @UiField
    LinkButton sendQuery;
    @UiField
    DropdownButton printAnchor;
    @UiField
    LinkButton upload;
    @UiField
    Label lockInformation;
    @UiField
    SplitLayoutPanel tablePanel;

    public static final int DEFAULT_TABLE_PANEL_TOP_POSITION = 34;
    private static final int LOCK_INFO_BLOCK_HEIGHT = 25;


    private boolean isVersion, isVersioned;
    private LinkButton printToExcel, printToCSV;
    private boolean uploadAvailable;

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

        printToExcel = new LinkButton("Сформировать XLSX");
        printToExcel.setHeight("20px");
        printToExcel.setDisableImage(true);
        printToExcel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getUiHandlers() != null) {
                    getUiHandlers().onPrintClicked(ReportType.EXCEL_REF_BOOK.getName());
                }
            }
        });

        printToCSV = new LinkButton("Сформировать CSV");
        printToCSV.setHeight("20px");
        printToCSV.setDisableImage(true);
        printToCSV.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getUiHandlers() != null) {
                    getUiHandlers().onPrintClicked(ReportType.CSV_REF_BOOK.getName());
                }
            }
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

    @UiHandler("upload")
    void showUploadDialog(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().showUploadDialogClicked();
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
        getUiHandlers().editClicked();
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

    @Override
    public void updateMode(FormMode mode) {
        switch (mode){
            case EDIT:
                addRow.setVisible(true);
                deleteRow.setVisible(true);
                separator.setVisible(true);
                edit.setVisible(false);
                search.setEnabled(true);
                exactSearch.setEnabled(true);
                filterText.setEnabled(true);
                relevanceDate.setEnabled(true);
                printAnchor.setVisible(false);
                upload.setVisible(uploadAvailable);
                break;
            case READ:
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                separator.setVisible(false);
                edit.setVisible(false);
                search.setEnabled(true);
                exactSearch.setEnabled(true);
                filterText.setEnabled(true);
                relevanceDate.setEnabled(true);
                printAnchor.setVisible(true);
                upload.setVisible(false);
                break;
            case VIEW:
                edit.setVisible(true);
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                separator.setVisible(false);
                search.setEnabled(true);
                exactSearch.setEnabled(true);
                filterText.setEnabled(true);
                relevanceDate.setEnabled(true);
                printAnchor.setVisible(true);
                upload.setVisible(false);
                break;
            case CREATE:
                addRow.setVisible(false);
                deleteRow.setVisible(false);
                search.setEnabled(false);
                exactSearch.setEnabled(false);
                filterText.setEnabled(false);
                separator.setVisible(false);
                relevanceDate.setEnabled(false);
                printAnchor.setVisible(false);
                upload.setVisible(false);
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
    public void setVersionView(boolean isVersion) {
        this.isVersion = isVersion;
        edit.setVisible(!isVersion);
        filterText.setEnabled(!isVersion);
        filterText.setVisible(!isVersion);
        backAnchor.setVisible(!isVersion);
        backToRefBookAnchor.setVisible(isVersion);
        backToRefBookAnchor.setText(titleDesc.getText());
        relevanceDate.setVisible(!isVersion&&isVersioned);
        relevanceDateLabel.setVisible(!isVersion&&isVersioned);
        separator.setVisible(!isVersion);
        search.setVisible(!isVersion);
        exactSearch.setVisible(!isVersion);
        separator1.setVisible(!isVersion&&isVersioned);
        cancelEdit.setVisible(!isVersion);
        printAnchor.setVisible(!isVersion);
    }

    @Override
    public void setIsVersion(boolean isVersioned) {
        this.isVersioned = isVersioned;
        relevanceDate.setVisible(isVersioned);
        relevanceDateLabel.setVisible(isVersioned);
        separator1.setVisible(isVersioned);
    }

    @Override
    public String getSearchPattern() {
        return filterText.getText();
    }

    @Override
    public Boolean getExactSearch() {
        return exactSearch.getValue();
    }

    @Override
    public void resetSearchInputBox() {
        filterText.setValue("");
    }

    @Override
    public void setSpecificReportTypes(List<String> specificReportTypes) {
        printAnchor.clear();
        printAnchor.addItem(ReportType.EXCEL_REF_BOOK.getName(), printToExcel);
        printAnchor.addItem(ReportType.CSV_REF_BOOK.getName(), printToCSV);
        for(final String specificReportType: specificReportTypes) {
            LinkButton linkButton = new LinkButton("Сформировать \"" + specificReportType + "\"");
            linkButton.setHeight("20px");
            linkButton.setDisableImage(true);
            linkButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (getUiHandlers() != null) {
                        getUiHandlers().onPrintClicked(specificReportType);
                    }
                }
            });
            printAnchor.addItem(specificReportType, linkButton);
        }
    }

    @Override
    public void setUploadAvailable(boolean uploadAvailable) {
        this.uploadAvailable = uploadAvailable;
    }

    @Override
    public void setLockInformation(String title) {
        if (title != null && !title.isEmpty()) {
            changeHierarchyPanelTopPosition(true);
            lockInformation.setVisible(true);
            lockInformation.setText(title);
            lockInformation.setTitle(title);
            addRow.setEnabled(false);
            deleteRow.setEnabled(false);
        } else {
            changeHierarchyPanelTopPosition(false);
            lockInformation.setVisible(false);
            lockInformation.setText("");
            lockInformation.setTitle("");
            addRow.setEnabled(true);
            deleteRow.setEnabled(true);
        }
    }

    /**
     * Увеличивает верхний отступ у панели с иерархией, когда показывается сообщение о блокировки
     * @param isLockInfoVisible показано ли сообщение
     */
    private void changeHierarchyPanelTopPosition(Boolean isLockInfoVisible){
        Style hierarchyPanelStyle = tablePanel.getElement().getStyle();
        int downShift = 0;
        if (isLockInfoVisible){
            downShift = LOCK_INFO_BLOCK_HEIGHT;
        }
        hierarchyPanelStyle.setProperty("top", DEFAULT_TABLE_PANEL_TOP_POSITION + downShift, Style.Unit.PX);
    }

}
