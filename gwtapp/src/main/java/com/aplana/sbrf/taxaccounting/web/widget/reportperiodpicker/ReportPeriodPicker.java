package com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker;


import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * Компонент для выбора отчетного периода.
 * Период может быть выбран один или несколько.
 */
public class ReportPeriodPicker extends Composite implements HasEnabled{

	interface SelectionUiBinder extends UiBinder<HTMLPanel, ReportPeriodPicker> {
	}

	@UiField
	TextBox selected;
	
	@UiField
	Button selectButton;

	private static final String ROOT_PANEL_WIDTH        = "250px";
	private static final String ROOT_PANEL_HEIGHT       = "250px";
	private static final String PANEL_WITH_TREE_WIDTH   = "250px";
	private static final String PANEL_WITH_TREE_HEIGHT  = "250px";
	private static final String RADIO_BUTTON_GROUP      = "DEPARTMENT_SELECTION";
	private final boolean multiselectTree;

	private static SelectionUiBinder uiBinder = GWT.create(SelectionUiBinder.class);
	private final ReportPeriodSelectHandler dataProvider;

	private final Label popupPanelLabel = new Label();
	private final Tree tree = new Tree();
	private final PopupPanel popup = new PopupPanel(true, false);
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final ScrollPanel panelWithTree = new ScrollPanel();
	private final HorizontalPanel buttonsPanel = new HorizontalPanel();
	private final Button applyButton = new Button("Выбрать");
    private final Button cancelButton = new Button("Отмена");

	private List<TaxPeriod> taxPeriods = new ArrayList<TaxPeriod>();
	private Map<Integer, ReportPeriodItem> reportPeriodItems = new HashMap<Integer, ReportPeriodItem>();
	private final Map<TaxPeriod, TreeItem> taxPeriodNodes = new HashMap<TaxPeriod, TreeItem>();
	private final Map<Integer, ReportPeriod> selectedReportPeriods = new HashMap<Integer, ReportPeriod>();
    private final Map<CheckBox, ReportPeriod> cbToReportPeriod = new HashMap<CheckBox, ReportPeriod>();
	private TaxPeriod lastTimeSelectedTaxPeriod = new TaxPeriod();

	public ReportPeriodPicker(ReportPeriodSelectHandler reportPeriodDataProvider){
		this(reportPeriodDataProvider, true);
	}

	@UiConstructor
	public ReportPeriodPicker(ReportPeriodSelectHandler reportPeriodDataProvider, boolean isMultiselect){
		initWidget(uiBinder.createAndBindUi(this));
		this.dataProvider = reportPeriodDataProvider;
		multiselectTree = isMultiselect;

		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                setSelectedReportPeriods();
            }
        });
		applyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				popup.hide();
			}
		});
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO Баг SBRFACCTAX-3258
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

		setupUI();
	}

	public Map<Integer, ReportPeriod> getSelectedReportPeriods(){
		return selectedReportPeriods;
	}

	public void setSelectedReportPeriods(List<ReportPeriod> reportPeriodList){
		if(reportPeriodList == null){
			clearSelected();
			return;
		}

		for(Map.Entry<Integer, ReportPeriodItem> entry : reportPeriodItems.entrySet()) {
			entry.getValue().getCheckBox().setValue(false);
		}

		Map<Integer, ReportPeriod> periods = new HashMap<Integer, ReportPeriod>();
		for(ReportPeriod item : reportPeriodList){
			if (item != null) {
				periods.put(item.getId(), item);
				if (reportPeriodItems != null && reportPeriodItems.get(item.getId()) != null) {
					reportPeriodItems.get(item.getId()).getCheckBox().setValue(true);
				}
			}
		}
		setSelectedReportPeriods(periods);
	}

	private void setSelectedReportPeriods(){
		StringBuilder result = new StringBuilder();
		StringBuilder tooltipTitle = new StringBuilder();
		for(Map.Entry<Integer, ReportPeriod> item : selectedReportPeriods.entrySet()){
			result.append(item.getValue().getName()).append(';');
			tooltipTitle.append(item.getValue().getName()).append('\n');
		}
		selected.setText(result.toString());
		selected.setTitle(tooltipTitle.toString());
        dataProvider.onReportPeriodsSelected(selectedReportPeriods);
	}

	private void setSelectedReportPeriods(Map<Integer, ReportPeriod> selectedReportPeriods){
		StringBuilder result = new StringBuilder();
		StringBuilder tooltipTitle = new StringBuilder();
		this.selectedReportPeriods.clear();
		this.selectedReportPeriods.putAll(selectedReportPeriods);
		for(Map.Entry<Integer, ReportPeriod> item : this.selectedReportPeriods.entrySet()){
			result.append(item.getValue().getName()).append(';');
			tooltipTitle.append(item.getValue().getName()).append('\n');
		}
		selected.setText(result.toString());
		selected.setTitle(tooltipTitle.toString());
	}

	private void clearSelected(){
		selected.setText("");
		selected.setTitle("");
		selectedReportPeriods.clear();
	}

	public void setTaxPeriods(List<TaxPeriod> taxPeriods){
		this.taxPeriods = taxPeriods;
		taxPeriodNodes.clear();
		tree.clear();
	}
	
	public void clearReportPeriods(){
		clearSelected();
		reportPeriodItems.clear();
        cbToReportPeriod.clear();
        for (TreeItem item : taxPeriodNodes.values()) {
			item.removeItems();
			item.addItem(new Label(""));
			item.setState(false);
		}
	}

	public void setReportPeriods(List<ReportPeriod> reportPeriods){
		for(ReportPeriod reportPeriod : reportPeriods){
			// Если период для ввода остатков, то добавляем к имени суффикс (ВО)
			//if (reportPeriod.isBalancePeriod()) {
			//	reportPeriod.setName(reportPeriod.getName() + " (ВО)");
			//}
			CheckBox checkBox;
			if(multiselectTree){
				checkBox = new CheckBox(reportPeriod.getName());
			} else {
				checkBox = new RadioButton(RADIO_BUTTON_GROUP, reportPeriod.getName());
			}
			if (selectedReportPeriods.containsKey(reportPeriod.getId())) {
				checkBox.setValue(true);
			}

			addValueChangeHandler(checkBox);
			ReportPeriodItem treeItem = new ReportPeriodItem(checkBox);
			treeItem.setReportPeriod(reportPeriod);
			reportPeriodItems.put(reportPeriod.getId(), treeItem);
            cbToReportPeriod.put(checkBox, reportPeriod);
			taxPeriodNodes.get(lastTimeSelectedTaxPeriod).addItem(treeItem);
		}
	}

	@UiHandler("selectButton")
	public void onClickSelectButton(ClickEvent event) {
		popup.setPopupPosition(getPopupLeftOffset(), getPopupTopOffset());
		if(!taxPeriodNodes.isEmpty()){
			popup.show();
			return;
		}
		Collections.reverse(taxPeriods);
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

	private int getPopupLeftOffset(){
		/*return (Window.getClientWidth() / 2) - 125;*/
        return selected.getAbsoluteLeft();
	}

	private int getPopupTopOffset(){
		/*return (Window.getClientHeight() / 2) - 125;*/
        return selected.getAbsoluteTop() + selected.getOffsetHeight();
	}

	private String getFormattedTaxPeriodDate(TaxPeriod taxPeriod){
		final String dateShortStart = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)
				.format(taxPeriod.getStartDate());
		final String dateShortEnd = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)
				.format(taxPeriod.getEndDate());
		int startDayIndex = dateShortStart.lastIndexOf('-');
		int startMonthIndex = dateShortStart.indexOf('-');
		int endDayIndex = dateShortEnd.lastIndexOf('-');
		int enMonthIndex = dateShortEnd.indexOf('-');
		String startDate =  dateShortStart.substring(startDayIndex + 1, dateShortStart.length()) + '.' +
							dateShortStart.substring(startMonthIndex + 1, startDayIndex) + '.' +
							dateShortStart.substring(0, startMonthIndex);
		String endDate =dateShortEnd.substring(endDayIndex + 1, dateShortEnd.length()) + '.' +
						dateShortEnd.substring(enMonthIndex + 1, endDayIndex) + '.' +
						dateShortEnd.substring(0, enMonthIndex);
		return (startDate + " - " + endDate);
	}

	private void setupUI(){
		rootPanel.setWidth(ROOT_PANEL_WIDTH);
		rootPanel.setHeight(ROOT_PANEL_HEIGHT);
		panelWithTree.setWidth(PANEL_WITH_TREE_WIDTH);
		panelWithTree.setHeight(PANEL_WITH_TREE_HEIGHT);
		popupPanelLabel.setText("Выберите отчетный период:");
		popupPanelLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);

		buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		applyButton.getElement().getStyle().setMarginLeft(120, Style.Unit.PX);
        cancelButton.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);
		buttonsPanel.add(applyButton);
        buttonsPanel.add(cancelButton);
		rootPanel.add(popupPanelLabel);
		rootPanel.add(panelWithTree);
		rootPanel.add(buttonsPanel);
		popup.add(rootPanel);
		popup.setPopupPosition(getPopupLeftOffset(), getPopupTopOffset());
		panelWithTree.add(tree);
	}

	private final class TaxPeriodItem extends TreeItem{
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

	private final class ReportPeriodItem extends TreeItem{
		private ReportPeriod reportPeriod;
		private CheckBox widget;
		private ReportPeriodItem(CheckBox widget){
			super(widget);
			this.widget = widget;
		}
		public ReportPeriod getReportPeriod() {
			return reportPeriod;
		}
		public CheckBox getCheckBox() {
			return widget;
		}
		public void setReportPeriod(ReportPeriod reportPeriod) {
			this.reportPeriod = reportPeriod;
		}
	}

	private void addValueChangeHandler(final CheckBox checkBox){
		checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {

                ReportPeriod checkedPeriod = cbToReportPeriod.get(event.getSource());

				if(checkBox.getValue()){
					if(checkBox instanceof RadioButton){
						selectedReportPeriods.clear();
					}
					selectedReportPeriods.put(checkedPeriod.getId(), checkedPeriod);
				} else if (!checkBox.getValue()) {
					selectedReportPeriods.remove(checkedPeriod.getId());
				}
			}
		});
	}

	@Override
	public boolean isEnabled() {
		return selectButton.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		selectButton.setEnabled(enabled);
	}
}
