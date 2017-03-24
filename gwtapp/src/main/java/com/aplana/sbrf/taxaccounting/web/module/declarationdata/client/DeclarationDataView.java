package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client.PdfViewerWidget;
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

    public static final String DATE_BOX_TITLE = "Дата и время создания формы";
    public static final String DATE_BOX_TITLE_D = "Дата и время создания формы";
    private static final int TABLE_TOP3 = 107;
    private static final int TABLE_TOP4 = 124;

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
	Button changeStatusEDButton;
	@UiField
	Anchor returnAnchor;
	@UiField
    LinkButton infoAnchor;
    @UiField
    LinkButton filesComments;

    @UiField
    Label declarationDataIdLabel;
    @UiField
    Label declarationDataId;

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
    SimplePanel propertyBlock;

    @UiField
    VerticalPanel leftAttrPanel, rightAttrPanel;

    @UiField
    Label stateED, stateEDLabel;

    @UiField
    Label createUserName, createUserNameLabel;

    private Label kpp, oktmo, taxOrganCode,asnu;
    private Label kppLabel, oktmoLabel, taxOrganCodeLabel, asnuLabel;

	@UiField
    PdfViewerWidget pdfViewer;
    @UiField
    DockLayoutPanel noPdfPanel;
    @UiField
    HTML noPdfLabel;

    @UiField
    Label dateBoxLabel, createDate;

    @UiField
    LinkButton sources;
    @UiField
    DropdownButton printAnchor;

    private LinkButton printToXml, downloadExcelButton;
    private HorizontalPanel printToExcelPanel;

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

        LinkButton printToExcel = new LinkButton("Сформировать в XLSX");
        printToExcel.setHeight("20px");
        printToExcel.setDisableImage(true);
        printToExcel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getUiHandlers() != null) {
                    getUiHandlers().viewReport(false, true, DeclarationDataReportType.EXCEL_DEC);
                }
            }
        });
        downloadExcelButton = new LinkButton(" (Скачать)");
        downloadExcelButton.setHeight("20px");
        downloadExcelButton.setDisableImage(true);
        downloadExcelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getUiHandlers() != null) {
                    getUiHandlers().viewReport(false, false, DeclarationDataReportType.EXCEL_DEC);
                }
            }
        });

        HTML separator = new HTML();
        separator.setWidth("5px");

        downloadExcelButton.setVisible(false);
        printToExcelPanel = new HorizontalPanel();
        printToExcelPanel.add(printToExcel);
        printToExcelPanel.add(separator);
        printToExcelPanel.add(downloadExcelButton);


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

        kppLabel = new Label("КПП:");
        kppLabel.addStyleName("headerAttrName");
        kpp = new Label("");
        kpp.addStyleName("headerAttr");
        kpp.addStyleName("depAttr");

        oktmoLabel = new Label("ОКТМО:");
        oktmoLabel.addStyleName("headerAttrName");
        oktmo = new Label("");
        oktmo.addStyleName("headerAttr");
        oktmo.addStyleName("depAttr");

        taxOrganCodeLabel = new Label("Код НО:");
        taxOrganCodeLabel.addStyleName("headerAttrName");
        taxOrganCode = new Label("");
        taxOrganCode.addStyleName("headerAttr");
        taxOrganCode.addStyleName("depAttr");

        asnuLabel = new Label("АСНУ:");
        asnuLabel.addStyleName("headerAttrName");
        asnu = new Label("");
        asnu.addStyleName("headerAttr");
        asnu.addStyleName("depAttr");
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
    public void showCheck(boolean show) {
        checkButton.setVisible(show);
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
	}

    @Override
    public void showDownloadButtons(boolean show) {
        printToExcelPanel.setVisible(show);
        printToXml.setVisible(show);
    }

	@Override
	public void showDelete(boolean show) {
		deleteButton.setVisible(show);
	}

    @Override
    public void showChangeStatusEDButton(boolean show) {
        changeStatusEDButton.setVisible(show);
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
    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId.setText(declarationDataId.toString());
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
    public void setCreateUserName(String userName) {
         createUserName.setText(userName);
         createUserName.setTitle(userName);
    }

    @Override
    public void setCreateDate(String createDate) {
        this.createDate.setText(createDate);
        this.createDate.setTitle(createDate);
    }

    @Override
    public void setPropertyBlockVisible(boolean isVisibleKpp, boolean isVisibleOktmo, boolean isVisibleTaxOrgan, boolean isVisibleStateED, boolean isVisibleAsnu, TaxType taxType) {
        leftAttrPanel.clear();
        rightAttrPanel.clear();

        if (isVisibleKpp) {
            leftAttrPanel.add(kppLabel);
            rightAttrPanel.add(kpp);
        }

        if (isVisibleKpp) {
            leftAttrPanel.add(oktmoLabel);
            rightAttrPanel.add(oktmo);
        }

        if (isVisibleTaxOrgan) {
            leftAttrPanel.add(taxOrganCodeLabel);
            rightAttrPanel.add(taxOrganCode);
        }

        if (isVisibleAsnu) {
            leftAttrPanel.add(asnuLabel);
            rightAttrPanel.add(asnu);
        }

        stateEDLabel.setVisible(isVisibleStateED);
        stateED.setVisible(isVisibleStateED);

        int propertyBlockSize = (isVisibleKpp?1:0) +
                (isVisibleOktmo?1:0) +
                (isVisibleTaxOrgan?1:0) +
                (isVisibleAsnu?1:0);

        int centerBlockSize = 3 + (isVisibleStateED?1:0);

        if (propertyBlockSize > centerBlockSize) {
            propertyBlock.getElement().getStyle().setOverflowY(Style.Overflow.SCROLL);
        } else {
            propertyBlock.getElement().getStyle().clearOverflowY();
        }

        int top;
        if (isVisibleStateED) {
            top = TABLE_TOP4;
            propertyBlock.getElement().getStyle().setHeight(78, Style.Unit.PX);
        } else {
            top = TABLE_TOP3;
            propertyBlock.getElement().getStyle().setHeight(58, Style.Unit.PX);
        }

        noPdfPanel.getElement().getStyle().setProperty("top", top, Style.Unit.PX);
        pdfViewer.getElement().getStyle().setProperty("top", top, Style.Unit.PX);
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

	@UiHandler("recalculateButton")
	public void onRecalculateButtonClicked(ClickEvent event){
        if (getUiHandlers() != null) {
            getUiHandlers().onRecalculateClicked(new Date(), false, false);
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

	@UiHandler("changeStatusEDButton")
	public void onChangeStatusED(ClickEvent event){
		getUiHandlers().changeStatusED();
	}

    @UiHandler("checkButton")
    public void onCheck(ClickEvent event){
        getUiHandlers().check(false);
    }

    @UiHandler("viewPdf")
    public void onViewPdfButton(ClickEvent event){
        if (isVisiblePDF) {
            getUiHandlers().viewReport(false, false, DeclarationDataReportType.PDF_DEC);
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
    public void updatePrintReportButtonName(DeclarationDataReportType type, boolean isLoad, String title) {
        if (DeclarationDataReportType.EXCEL_DEC.equals(type)) {
            if (isLoad) {
                downloadExcelButton.setVisible(true);
                downloadExcelButton.setTitle(title);
                timerExcel.cancel();
            } else {
                downloadExcelButton.setVisible(false);
                downloadExcelButton.setTitle("");
            }
        } else if (DeclarationDataReportType.XML_DEC.equals(type)) {
            printToExcelPanel.setVisible(false);
            if (isLoad) {
                printToExcelPanel.setVisible(true);
                printToXml.setVisible(true);
                printAnchor.setEnabled(true);
                timerXML.cancel();
                timerSpecific.scheduleRepeating(10000);
                timerSpecific.run();
            } else {
                viewPdf.setVisible(false);
                printToXml.setVisible(false);
                printToExcelPanel.setVisible(false);
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
        printAnchor.addItem(DeclarationDataReportType.EXCEL_DEC.getReportType().getName(), printToExcelPanel);

        for(final DeclarationSubreport subreport: subreports) {
            LinkButton createButton = new LinkButton("Сформировать \"" + subreport.getName() + "\"");
            createButton.setHeight("20px");
            createButton.setDisableImage(true);
            createButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (getUiHandlers() != null) {
                        getUiHandlers().viewReport(false, true, new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, subreport));
                    }
                }
            });

            HTML separator = new HTML();
            separator.setWidth("5px");

            LinkButton downloadButton = new LinkButton(" (Скачать)");
            downloadButton.setHeight("20px");
            downloadButton.setDisableImage(true);
            downloadButton.setVisible(false);
            downloadButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (getUiHandlers() != null) {
                        getUiHandlers().viewReport(false, false, new DeclarationDataReportType(ReportType.SPECIFIC_REPORT_DEC, subreport));
                    }
                }
            });

            HorizontalPanel horizontalPanel = new HorizontalPanel();
            horizontalPanel.add(createButton);
            horizontalPanel.add(separator);
            horizontalPanel.add(downloadButton);
            printAnchor.addItem(subreport.getAlias(), horizontalPanel);
        }
    }

    @Override
    public void updatePrintSubreportButtonName(DeclarationSubreport subreport, boolean exist, String title) {
        LinkButton downloadButton = (LinkButton)((HorizontalPanel) printAnchor.getItem(subreport.getAlias())).getWidget(2);
        if (downloadButton != null) {
            if (exist) {
                downloadButton.setTitle(title);
                downloadButton.setVisible(true);
            } else {
                downloadButton.setTitle("");
                downloadButton.setVisible(false);
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