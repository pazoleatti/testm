package com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker;


import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReportPeriodPicker extends Composite{

	interface SelectionUiBinder extends UiBinder<HTMLPanel, ReportPeriodPicker> {
	}

	@UiField
	TextBox selected;

	private static final String ROOT_PANEL_WIDTH        = "250px";
	private static final String ROOT_PANEL_HEIGHT       = "250px";
	private static final String PANEL_WITH_TREE_WIDTH   = "250px";
	private static final String PANEL_WITH_TREE_HEIGHT  = "250px";
	private static final String APPLY_BUTTON_WIDTH      = "50px";

	private static SelectionUiBinder uiBinder = GWT.create(SelectionUiBinder.class);
	private final ReportPeriodDataProvider dataProvider;

	private final Label popupPanelLabel = new Label();
	private final Tree tree = new Tree();
	private final PopupPanel popup = new PopupPanel(true, false);
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final ScrollPanel panelWithTree = new ScrollPanel();
	private final HorizontalPanel buttonsPanel = new HorizontalPanel();
	private final Button applyButton = new Button("ОК");

	private List<TaxPeriod> taxPeriods = new ArrayList<TaxPeriod>();
	private final Map<TaxPeriod, TreeItem> taxPeriodNodes = new HashMap<TaxPeriod, TreeItem>();
	private final Map<Integer, String> selectedReportPeriods = new HashMap<Integer, String>();
	private TaxPeriod lastTimeSelectedTaxPeriod = new TaxPeriod();

	@UiConstructor
	public ReportPeriodPicker(ReportPeriodDataProvider reportPeriodDataProvider){
		initWidget(uiBinder.createAndBindUi(this));
		this.dataProvider = reportPeriodDataProvider;
		applyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				StringBuilder result = new StringBuilder();
				for(Map.Entry<Integer, String> item : selectedReportPeriods.entrySet()){
					result.append(item.getValue()).append(";");
				}
				selected.setText(result.toString());
				popup.hide();
			}
		});
		tree.addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				lastTimeSelectedTaxPeriod = ((TaxPeriodItem) event.getTarget()).getTaxPeriod();
				if (taxPeriodNodes.get(lastTimeSelectedTaxPeriod).getChildCount() == 1) { // 1 => т.к. мы вставляли Фэйковую ноду для того чтобы значок "+" отображался слева от пустой ноды
					dataProvider.onTaxPeriodSelected(lastTimeSelectedTaxPeriod);
				}
			}
		});
		tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				if(event.getSelectedItem() instanceof ReportPeriodItem){
					ReportPeriodItem selectedItem = (ReportPeriodItem)event.getSelectedItem();
					if(!selectedReportPeriods.containsKey(selectedItem.getReportPeriod().getId())){
						selectedReportPeriods.put(selectedItem.getReportPeriod().getId(), selectedItem.getReportPeriod().getName());
					} else {
						selectedReportPeriods.remove(selectedItem.getReportPeriod().getId());
					}
				}
			}
		});
		setupUI();
	}

	public Map<Integer, String> getSelectedReportPeriods(){
		return selectedReportPeriods;
	}

	public void setTaxPeriods(List<TaxPeriod> taxPeriods){
		this.taxPeriods = taxPeriods;
	}

	public void setReportPeriods(List<ReportPeriod> reportPeriods){
		for(ReportPeriod reportPeriod : reportPeriods){
			CheckBox checkBox = new CheckBox(reportPeriod.getName());
			ReportPeriodItem treeItem = new ReportPeriodItem(checkBox);
			treeItem.setReportPeriod(reportPeriod);
			taxPeriodNodes.get(lastTimeSelectedTaxPeriod).addItem(treeItem);
		}

	}

	@UiHandler("selectButton")
	public void onClickSelectButton(ClickEvent event) {
		if(!taxPeriodNodes.isEmpty()){
			popup.show();
			return;
		}
		for(TaxPeriod taxPeriod : taxPeriods){
			Label label = new Label(getFormattedTaxPeriodDate(taxPeriod));
			TaxPeriodItem element = new TaxPeriodItem(label);
			element.setTaxPeriod(taxPeriod);
			element.addItem(new Label("")); // Фэйковая нода нужна для того чтобы значок "+" отображался слева от пустой ноды
			tree.addItem(element);
			taxPeriodNodes.put(taxPeriod, element);
		}
		popup.show();
	}

	private String getFormattedTaxPeriodDate(TaxPeriod taxPeriod){
		final String DATE_SHORT_START = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)
				.format(taxPeriod.getStartDate());
		final String DATE_SHORT_END = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)
				.format(taxPeriod.getEndDate());
		int startDayIndex = DATE_SHORT_START.lastIndexOf("-");
		int startMonthIndex = DATE_SHORT_START.indexOf("-");
		int endDayIndex = DATE_SHORT_END.lastIndexOf("-");
		int enMonthIndex = DATE_SHORT_END.indexOf("-");
		String startDate =  DATE_SHORT_START.substring(startDayIndex + 1, DATE_SHORT_START.length()) + "." +
							DATE_SHORT_START.substring(startMonthIndex + 1, startDayIndex) + "." +
							DATE_SHORT_START.substring(0, startMonthIndex);
		String endDate =DATE_SHORT_END.substring(endDayIndex + 1, DATE_SHORT_END.length()) + "." +
						DATE_SHORT_END.substring(enMonthIndex + 1, endDayIndex) + "." +
						DATE_SHORT_END.substring(0, enMonthIndex);
		return (startDate + " - " + endDate);
	}

	private void setupUI(){
		rootPanel.setWidth(ROOT_PANEL_WIDTH);
		rootPanel.setHeight(ROOT_PANEL_HEIGHT);
		panelWithTree.setWidth(PANEL_WITH_TREE_WIDTH);
		panelWithTree.setHeight(PANEL_WITH_TREE_HEIGHT);
		applyButton.setWidth(APPLY_BUTTON_WIDTH);
		applyButton.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
		popupPanelLabel.setText("Выберите отчетный период:");
		popupPanelLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);

		buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		applyButton.getElement().getStyle().setMarginLeft(200, Style.Unit.PX);
		buttonsPanel.add(applyButton);
		rootPanel.add(popupPanelLabel);
		rootPanel.add(panelWithTree);
		rootPanel.add(buttonsPanel);
		popup.add(rootPanel);
		panelWithTree.add(tree);
	}

	private class TaxPeriodItem extends TreeItem{
		private TaxPeriodItem(Widget widget) {
			super(widget);
		}
		private TaxPeriod taxPeriod;
		public TaxPeriod getTaxPeriod() {
			return taxPeriod;
		}
		public void setTaxPeriod(TaxPeriod taxPeriod) {
			this.taxPeriod = taxPeriod;
		}
	}

	private class ReportPeriodItem extends TreeItem{
		private ReportPeriodItem(Widget widget){
			super(widget);
		}
		private ReportPeriod reportPeriod;
		public ReportPeriod getReportPeriod() {
			return reportPeriod;
		}
		public void setReportPeriod(ReportPeriod reportPeriod) {
			this.reportPeriod = reportPeriod;
		}
	}
}
