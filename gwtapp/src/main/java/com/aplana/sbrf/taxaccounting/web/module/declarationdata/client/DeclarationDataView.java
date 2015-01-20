package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client.PdfViewerView;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;

public class DeclarationDataView extends ViewWithUiHandlers<DeclarationDataUiHandlers>
		implements DeclarationDataPresenter.MyView{

	interface Binder extends UiBinder<Widget, DeclarationDataView> { }

    public static final String DATE_BOX_TITLE = "Дата формирования декларации";
    public static final String DATE_BOX_TITLE_D = "Дата формирования уведомления";

	@UiField
	Button recalculateButton;
	@UiField
	Button acceptButton;
	@UiField
	Button cancelButton;
	@UiField
	Anchor downloadExcelButton;
	@UiField
	Anchor downloadXmlButton;
	@UiField
	Button deleteButton;
	@UiField
	Button checkButton;
	@UiField
	Anchor returnAnchor;
	@UiField
    LinkButton infoAnchor;

    @UiField
    Label typeLabel;

	@UiField
	Label type;
	@UiField
	Label reportPeriod;
	@UiField
	Label department;
	@UiField
	Label stateLabel;
	@UiField
	Label title;

	@UiField
    HorizontalPanel propertyBlock;
    @UiField
    Label taxOrganCode;
    @UiField
    Label kpp;
    @UiField
    Label taxOrganCodeLabel;
    @UiField
    Label kppLabel;

	@UiField
	PdfViewerView pdfViewer;
    @UiField
    DockLayoutPanel noPdfPanel;
    @UiField
    HTML noPdfLabel;

    @UiField
    Label dateBoxLabel;

	@UiField
    DateMaskBoxPicker dateBox;

    private Timer timerExcel, timerXML;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
        timerExcel = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().onTimerReport(ReportType.EXCEL_DEC, true);
                } catch (Exception e) {
                    //Nothing
                }
            }
        };

        timerXML = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().onTimerReport(ReportType.XML_DEC, true);
                } catch (Exception e) {
                    //Nothing
                }
            }
        };

        timerExcel.cancel();
        timerXML.cancel();
	}

    @Override
    public void showState(boolean accepted) {
        String status;
        boolean isDeal = getUiHandlers().getTaxType().equals(TaxType.DEAL);
        if (accepted) {
            status = isDeal ? "Принято" : "Принята";
        }else{
            status =  isDeal ? "Создано" : "Создана";
        }
        stateLabel.setText(status);
    }

    @Override
    public void showNoPdf(String text) {
        pdfViewer.setVisible(false);
        noPdfPanel.setVisible(true);
        noPdfLabel.setText(text);
    }

    @Override
	public void showAccept(boolean show) {
		acceptButton.setVisible(show);
	}

	@Override
	public void showReject(boolean show) {
		cancelButton.setVisible(show);
	}

	@Override
	public void showRecalculateButton(boolean show) {
		recalculateButton.setVisible(show);
		dateBox.setEnabled(show);
	}

    @Override
    public void showDownloadButtons(boolean show) {
        downloadExcelButton.setVisible(show);
        downloadXmlButton.setVisible(show);
    }

	@Override
	public void showDelete(boolean show) {
		deleteButton.setVisible(show);
	}

	@Override
	public void setType(String type) {
		this.type.setText(type);
	}

	@Override
	public void setTitle(String title, boolean isTaxTypeDeal) {
		this.title.setText(title);
		this.title.setTitle(title);
        type.setVisible(!isTaxTypeDeal);
        typeLabel.setVisible(!isTaxTypeDeal);
        if (!isTaxTypeDeal) {
            dateBoxLabel.setText(DATE_BOX_TITLE + ":");
            dateBoxLabel.setTitle(DATE_BOX_TITLE);
        } else {
            dateBoxLabel.setText(DATE_BOX_TITLE_D + ":");
            dateBoxLabel.setTitle(DATE_BOX_TITLE_D);
        }
	}

	@Override
	public void setDepartment(String department) {
		this.department.setText(department);
		this.department.setTitle(department);
	}

    @Override
    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode.setText(taxOrganCode);
        this.taxOrganCode.setTitle(taxOrganCode);
    }

    @Override
    public void setKpp(String kpp) {
        this.kpp.setText(kpp);
        this.kpp.setTitle(kpp);
    }

    @Override
    public void setPropertyBlockVisible(boolean isVisibleTaxOrgan, boolean isVisibleKpp) {
        taxOrganCode.setVisible(isVisibleTaxOrgan);
        taxOrganCodeLabel.setVisible(isVisibleTaxOrgan);
        kpp.setVisible(isVisibleKpp);
        kppLabel.setVisible(isVisibleKpp);
    }

    @Override
    public void setPdfPage(int page) {
        pdfViewer.setPage(page);
    }

    @Override
	public void setReportPeriod(String reportPeriod) {
		this.reportPeriod.setText(reportPeriod);
	}

    @Override
    public void setBackButton(String link, String text) {
        returnAnchor.setHref(link);
        returnAnchor.setText(text);
    }

	@Override
	public void setPdf(Pdf pdf) {
        pdfViewer.setVisible(true);
        noPdfPanel.setVisible(false);
		pdfViewer.setPages(pdf);
	}

	@Override
	public void setDocDate(Date date) {
		dateBox.setValue(date);
	}

	@UiHandler("recalculateButton")
	public void onRecalculateButtonClicked(ClickEvent event){
        if (dateBox.getValue() == null) {
            Dialog.warningMessage("Введите дату.");
        } else {
            if (getUiHandlers() != null) {
                getUiHandlers().onRecalculateClicked(dateBox.getValue());
            }
        }
    }

	@UiHandler("acceptButton")
	public void onAccept(ClickEvent event){
		getUiHandlers().accept(true);
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		getUiHandlers().accept(false);
	}

	@UiHandler("deleteButton")
	public void onDelete(ClickEvent event){
		getUiHandlers().delete();
	}

	@UiHandler("checkButton")
	public void onCheck(ClickEvent event){
		getUiHandlers().check();
	}

	@UiHandler("downloadExcelButton")
	public void onDownloadExcelButton(ClickEvent event){
		getUiHandlers().downloadExcel();
	}

	@UiHandler("downloadXmlButton")
	public void onDownloadAsLegislatorButton(ClickEvent event){
		getUiHandlers().downloadXml();
	}

	@UiHandler("infoAnchor")
	void onInfoButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onInfoClicked();
		}
	}

    @Override
    public void updatePrintReportButtonName(ReportType reportType, boolean isLoad) {
        if (ReportType.EXCEL_DEC.equals(reportType)) {
            if (isLoad) {
                downloadExcelButton.setText("Выгрузить в xlsx");
                timerExcel.cancel();
            } else {
                downloadExcelButton.setText("Сформировать xlsx");
            }
        } else {
            if (isLoad) {
                downloadXmlButton.setVisible(true);
                downloadXmlButton.setText("Выгрузить в XML");
                downloadExcelButton.setVisible(true);
                getUiHandlers().onTimerReport(ReportType.EXCEL_DEC, false);
                timerXML.cancel();
            } else {
                downloadXmlButton.setVisible(false);
                downloadXmlButton.setText("Сформировать XML");
                downloadExcelButton.setVisible(false);
            }
        }
    }

    @Override
    public void startTimerReport(ReportType reportType) {
        if (ReportType.EXCEL_DEC.equals(reportType)) {
            timerExcel.scheduleRepeating(10000);
            timerExcel.run();
        } else {
            timerXML.scheduleRepeating(10000);
            timerXML.run();
        }
    }

    @Override
    public void stopTimerReport(ReportType reportType) {
        if (ReportType.EXCEL_DEC.equals(reportType)) {
            timerExcel.cancel();
        } else {
            timerXML.cancel();
        }
    }

}