package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client.PdfViewerView;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.style.DropdownButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.dom.client.Style;
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

    public static final String DATE_BOX_TITLE = "Дата формирования налоговой формы";
    public static final String DATE_BOX_TITLE_D = "Дата формирования уведомления";
    private static final int TABLE_TOP2 = 103;
    private static final int TABLE_TOP3 = 108;
    private static final int TABLE_TOP4 = 125;
    private static final int TABLE_TOP5 = 142;

	@UiField
	Button recalculateButton;
	@UiField
	Button acceptButton;
	@UiField
	Button cancelButton;
    @UiField
    Button viewPdf;
	@UiField
	Button deleteButton;
	@UiField
	Button checkButton;
	@UiField
	Anchor returnAnchor;
	@UiField
    LinkButton infoAnchor;
    @UiField
    LinkButton filesComments;

    @UiField
    Label formKindLabel;
    @UiField
    Label formKind;
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
    Label kpp, oktmo, taxOrganCode, stateED, asnu, importTf;
    @UiField
    Label kppLabel, oktmoLabel, taxOrganCodeLabel, stateEDLabel, asnuLabel, importTfLabel;

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

    private Timer timerExcel, timerXML, timerPDF, timerAccept, timerSpecific, timerCheck;
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

        timerCheck = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().onTimerReport(DeclarationDataReportType.CHECK_DEC, true);
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
        timerCheck.cancel();
	}

    @Override
    public void showState(State state) {
        stateLabel.setText(state.getTitle());
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
//		this.formType.setText(type);
	}

    @Override
    public void setFormKind(String formKind) {
        this.formKind.setText(formKind);
    }

    @Override
	public void setTitle(String title, boolean isTaxTypeDeal) {
		this.title.setText(title);
		this.title.setTitle(title);
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
    public void setKpp(String kpp) {
        this.kpp.setText(kpp);
        this.kpp.setTitle(kpp);
    }

    @Override
    public void setOktmo(String oktmo) {
        this.oktmo.setText(oktmo);
        this.oktmo.setTitle(oktmo);
    }

    @Override
    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode.setText(taxOrganCode);
        this.taxOrganCode.setTitle(taxOrganCode);
    }

    @Override
    public void setStateED(String stateED) {
        this.stateED.setText(stateED);
        this.stateED.setTitle(stateED);

    }

    @Override
    public void setFileName(String guid) {
    }

    @Override
    public void setAsnuName(String asnuName) {
        this.asnu.setText(asnuName);
        this.asnu.setTitle(asnuName);
    }

    @Override
    public void setImportTf(String userName) {
        importTf.setText(userName);
        importTfLabel.setTitle(userName);
    }

    @Override
    public void setPropertyBlockVisible(boolean isVisibleKpp, boolean isVisibleOktmo, boolean isVisibleTaxOrgan, boolean isVisibleStateED, boolean isVisibleAsnu, boolean isVisibleImportTf, TaxType taxType) {
        kpp.setVisible(isVisibleKpp);
        kppLabel.setVisible(isVisibleKpp);

        oktmo.setVisible(isVisibleOktmo);
        oktmoLabel.setVisible(isVisibleOktmo);

        taxOrganCode.setVisible(isVisibleTaxOrgan);
        taxOrganCodeLabel.setVisible(isVisibleTaxOrgan);

        stateED.setVisible(isVisibleStateED);
        stateEDLabel.setVisible(isVisibleStateED);

        asnu.setVisible(isVisibleAsnu);
        asnuLabel.setVisible(isVisibleAsnu);

        importTf.setVisible(isVisibleImportTf);
        importTfLabel.setVisible(isVisibleImportTf);

        int num = (isVisibleKpp?1:0) +
                (isVisibleOktmo?1:0) +
                (isVisibleTaxOrgan?1:0) +
                (isVisibleStateED?1:0) +
                (isVisibleAsnu?1:0) +
                (isVisibleImportTf?1:0);

        int top = (num == 5) ? TABLE_TOP5 :  (num == 4) ? TABLE_TOP4 :
                  ((num == 3) ? TABLE_TOP3 : TABLE_TOP2);
        noPdfPanel.getElement().getStyle().setProperty("top", top, Style.Unit.PX);
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
        if (isVisiblePDF) {
            getUiHandlers().viewReport(false, DeclarationDataReportType.PDF_DEC);
        }
    }

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
            if (!isLoad) {
                getUiHandlers().revealPlaceRequest();
                timerAccept.cancel();
            }
        } else if (DeclarationDataReportType.CHECK_DEC.equals(type)) {
            if (!isLoad) {
                getUiHandlers().revealPlaceRequest();
                timerCheck.cancel();
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
        } else if (DeclarationDataReportType.CHECK_DEC.equals(type)) {
            timerCheck.scheduleRepeating(10000);
            timerCheck.run();
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
        } else if (DeclarationDataReportType.CHECK_DEC.equals(type)) {
            timerCheck.cancel();
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

    @UiHandler("filesComments")
    public void onFilesCommentsClicked(ClickEvent event){
        getUiHandlers().onFilesCommentsDialog();
    }
}