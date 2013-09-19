package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewImpl;

import java.util.List;
import java.util.Map;

public class HistoryView extends PopupViewImpl implements
		HistoryPresenter.MyView {

	interface Binder extends UiBinder<PopupPanel, HistoryView> {
	}

	private final PopupPanel widget;
	private Map<Integer, String> userNames;
	private Map<Integer, String> userDepartments;
	private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");
	private static final String DECLARATION_SAVE_EVENT = "Обновление";

	@UiField
	DataGrid<LogBusiness> logsTable;

	@UiField
	Button hideButton;

	@Inject
	public HistoryView(EventBus eventBus, Binder uiBinder) {
		super(eventBus);
		widget = uiBinder.createAndBindUi(this);
		initTable();
	}

	@Override
	public void setHistory(List<LogBusiness> logs, Map<Integer, String> userNames, Map<Integer, String> userDepartments) {
		this.userNames = userNames;
		this.userDepartments = userDepartments;
		logsTable.setRowData(logs);
		logsTable.redraw();
	}

	@Override
	public PopupPanel asWidget() {
		return widget;
	}

	private void initTable() {
		TextColumn<LogBusiness> eventColumn = new TextColumn<LogBusiness>() {
			@Override
			public String getValue(LogBusiness object) {
				FormDataEvent event = FormDataEvent.getByCode(object.getEventId());
				if (object.getDeclarationId() != null && FormDataEvent.SAVE == event) {
					return DECLARATION_SAVE_EVENT;
				}
				return event.getTitle();
			}
		};

		TextColumn<LogBusiness> dateColumn = new TextColumn<LogBusiness>() {
			@Override
			public String getValue(LogBusiness object) {
				return format.format(object.getLogDate());
			}
		};

		TextColumn<LogBusiness> nameColumn = new TextColumn<LogBusiness>() {
			@Override
			public String getValue(LogBusiness object) {
				return userNames.get(object.getUserId());
			}
		};

		TextColumn<LogBusiness> rolesColumn = new TextColumn<LogBusiness>() {
			@Override
			public String getValue(LogBusiness object) {
				return object.getRoles();
			}
		};

		TextColumn<LogBusiness> departmentColumn = new TextColumn<LogBusiness>() {
			@Override
			public String getValue(LogBusiness object) {
				return userDepartments.get(object.getDepartmentId());
			}
		};

		TextColumn<LogBusiness> noteColumn = new TextColumn<LogBusiness>() {
			@Override
			public String getValue(LogBusiness object) {
				return object.getNote();
			}
		};

		logsTable.addColumn(eventColumn, "Событие");
		logsTable.addColumn(dateColumn, "Дата-время");
		logsTable.addColumn(nameColumn, "Пользователь");
		logsTable.addColumn(rolesColumn, "Роли пользователя");
		logsTable.addColumn(departmentColumn, "Подразделение пользователя");
		logsTable.addColumn(noteColumn, "Текст события");
		logsTable.addCellPreviewHandler(new CellPreviewEvent.Handler<LogBusiness>() {
			@Override
			public void onCellPreview(CellPreviewEvent<LogBusiness> event) {
				if ("mouseover".equals(event.getNativeEvent().getType())) {
					long index = event.getIndex();
					TableCellElement cellElement = logsTable.getRowElement((int) index).getCells().getItem(event.getColumn());
					if (cellElement.getInnerText().replace("\u00A0", "").trim().isEmpty()) {
						cellElement.removeAttribute("title");
					} else {
						cellElement.setTitle(cellElement.getInnerText());
					}
				}
			}
		});
	}

	@UiHandler("hideButton")
	public void onHideButton(ClickEvent event) {
		hide();
	}

}
