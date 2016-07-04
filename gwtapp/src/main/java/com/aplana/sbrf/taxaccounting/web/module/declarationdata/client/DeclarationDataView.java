package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client.PdfViewerView;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.style.DropdownButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;
import java.util.List;

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
    Button viewPdf;
	//@UiField
	//Anchor downloadExcelButton;
	//@UiField
	//Anchor downloadXmlButton;
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
    @UiField
    LinkButton sources;
    @UiField
    DropdownButton printAnchor;

    private LinkButton printToXml, printToExcel;

    private Timer timerExcel, timerXML, timerPDF, timerAccept, timerSpecific;
    private boolean isVisiblePDF;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
        printToXml = new LinkButton("Выгрузить в XML");
        printToXml.setHeight("20px");
        printToXml.setDisableImage(true);
        printToXml.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getUiHandlers() != null) {
                    getUiHandlers().downloadXml();
                }
            }
        });

        printToExcel = new LinkButton("Сформировать в XLSX");
        printToExcel.setHeight("20px");
        printToExcel.setDisableImage(true);
        printToExcel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getUiHandlers() != null) {
                    getUiHandlers().viewReport(false, DeclarationDataReportType.EXCEL_DEC);
                }
            }
        });

        timerExcel = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().onTimerReport(DeclarationDataReportType.EXCEL_DEC, true);
                } catch (Exception e) {
                    //Nothing
                }
            }
        };

        timerXML = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().onTimerReport(DeclarationDataReportType.XML_DEC, true);
                } catch (Exception e) {
                    //Nothing
                }
            }
        };

        timerPDF = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().onTimerReport(DeclarationDataReportType.PDF_DEC, true);
                } catch (Exception e) {
                }
            }
        };

        timerAccept = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().onTimerReport(DeclarationDataReportType.ACCEPT_DEC, true);
                } catch (Exception e) {
                }
            }
        };

        timerSpecific = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().onTimerSubsreport(true);
                } catch (Exception e) {
                }
            }
        };

        timerExcel.cancel();
        timerXML.cancel();
        timerPDF.cancel();
        timerAccept.cancel();
        timerSpecific.cancel();
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
        printToExcel.setVisible(show);
        printToXml.setVisible(show);
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
    public void setPropertyBlockVisible(boolean isVisibleTaxOrgan, boolean isVisibleKpp, TaxType taxType) {
        taxOrganCode.setVisible(isVisibleTaxOrgan);
        taxOrganCodeLabel.setVisible(isVisibleTaxOrgan);
        kpp.setVisible(isVisibleKpp);
        kppLabel.setVisible(isVisibleKpp);

        if (taxType == TaxType.TRANSPORT) {
            taxOrganCodeLabel.setText("Налоговый орган (кон.):");
        } else {
            taxOrganCodeLabel.setText("Налоговый орган:");
        }
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
                getUiHandlers().onRecalculateClicked(dateBox.getValue(), false, false);
            }
        }
    }

	@UiHandler("acceptButton")
	public void onAccept(ClickEvent event){
		getUiHandlers().accept(true, false, false);
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		getUiHandlers().accept(false, false, false);
	}

	@UiHandler("deleteButton")
	public void onDelete(ClickEvent event){
		getUiHandlers().delete();
	}

	@UiHandler("checkButton")
	public void onCheck(ClickEvent event){
		getUiHandlers().check(false);
	}

    @UiHandler("viewPdf")
    public void onViewPdfButton(ClickEvent event){
        if (isVisiblePDF)
            getUiHandlers().viewReport(false, DeclarationDataReportType.PDF_DEC);
        //getUiHandlers().viewPdf(false);
    }
/*
	@UiHandler("downloadExcelButton")
	public void onDownloadExcelButton(ClickEvent event){
        getUiHandlers().viewReport(false, DeclarationDataReportType.EXCEL_DEC);
		//getUiHandlers().downloadExcel();
	}


	@UiHandler("downloadXmlButton")
	public void onDownloadAsLegislatorButton(ClickEvent event){
		getUiHandlers().downloadXml();
	}*/

	@UiHandler("infoAnchor")
	void onInfoButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onInfoClicked();
		}
	}
    @UiHandler("sources")
    public void onSourcesClicked(ClickEvent event){
        getUiHandlers().onOpenSourcesDialog();
    }

    @Override
    public void updatePrintReportButtonName(DeclarationDataReportType type, boolean isLoad) {
        if (DeclarationDataReportType.EXCEL_DEC.equals(type)) {
            if (isLoad) {
                printToExcel.setText("Выгрузить в xlsx");
                timerExcel.cancel();
            } else {
                printToExcel.setText("Сформировать xlsx");
            }
        } else if (DeclarationDataReportType.XML_DEC.equals(type)) {
            printToExcel.setVisible(false);
            if (isLoad) {
                printToExcel.setVisible(true);
                printToXml.setVisible(true);
                printAnchor.setEnabled(true);
                timerXML.cancel();
                timerSpecific.scheduleRepeating(10000);
                timerSpecific.run();
            } else {
                viewPdf.setVisible(false);
                printToXml.setVisible(false);
                printToExcel.setVisible(false);
                printAnchor.setEnabled(false);
                timerSpecific.cancel();
            }
        } else if (DeclarationDataReportType.PDF_DEC.equals(type)) {
            if (isLoad) {
                viewPdf.setVisible(false);
                timerPDF.cancel();
            } else {
                viewPdf.setVisible(isVisiblePDF);
            }
        } else if (DeclarationDataReportType.ACCEPT_DEC.equals(type)) {
            if (isLoad) {
                getUiHandlers().revealPlaceRequest();
                timerAccept.cancel();
            } else {

            }
        }
    }

    @Override
    public void startTimerReport(DeclarationDataReportType type) {
        if (DeclarationDataReportType.EXCEL_DEC.equals(type)) {
            timerExcel.scheduleRepeating(10000);
            timerExcel.run();
        } else if (DeclarationDataReportType.XML_DEC.equals(type)) {
            timerXML.scheduleRepeating(10000);
            timerXML.run();
        } else if (DeclarationDataReportType.PDF_DEC.equals(type)) {
            if (isVisiblePDF) {
                timerPDF.scheduleRepeating(10000);
                timerPDF.run();
            }
        } else if (DeclarationDataReportType.ACCEPT_DEC.equals(type)) {
            timerAccept.scheduleRepeating(10000);
            timerAccept.run();
        }
    }

    @Override
    public void stopTimerReport(DeclarationDataReportType type) {
        if (DeclarationDataReportType.EXCEL_DEC.equals(type)) {
            timerExcel.cancel();
        } else if (DeclarationDataReportType.XML_DEC.equals(type)) {
            timerXML.cancel();
        } else if (DeclarationDataReportType.PDF_DEC.equals(type)) {
            timerPDF.cancel();
        } else if (DeclarationDataReportType.ACCEPT_DEC.equals(type)) {
            timerAccept.cancel();
        } else if (type.isSubreport()) {
            timerSpecific.cancel();
        }
    }

    @Override
    public boolean getVisiblePdfViewer() {
        return !noPdfPanel.isVisible();
    }

    @Override
    public void setSubreports(List<DeclarationSubreport> subreports) {
        printAnchor.clear();
        printAnchor.addItem(DeclarationDataReportType.XML_DEC.getReportType().getName(), printToXml);
        printAnchor.addItem(DeclarationDataReportType.EXCEL_DEC.getReportType().getName(), printToExcel);

        for(final DeclarationSubreport subreport: subreports) {
            LinkButton linkButton = new LinkButton("Сформировать \"" + subreport.getName() + "\"");
            linkButton.setHeight("20px");
            linkButton.setDisableImage(true);
            linkButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (getUiHandlers() != null) {
                        getUiHandlers().viewReport(false, new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, subreport));
                    }
                }
            });
            printAnchor.addItem(subreport.getAlias(), linkButton);
        }
    }

    public void updatePrintSubreportButtonName(DeclarationSubreport subreport, boolean exist) {
        LinkButton linkButton = (LinkButton) printAnchor.getItem(subreport.getAlias());
        if (linkButton != null) {
            if (exist) {
                linkButton.setText("Выгрузить \"" + subreport.getName() + "\"");
            } else {
                linkButton.setText("Сформировать \"" + subreport.getName() + "\"");
            }
        }
    }

    @Override
    public void setVisiblePDF(boolean isVisiblePDF) {
        this.isVisiblePDF = isVisiblePDF;
        viewPdf.setVisible(isVisiblePDF);
    }

    @Override
    public boolean isVisiblePDF() {
        return isVisiblePDF;
    }
}