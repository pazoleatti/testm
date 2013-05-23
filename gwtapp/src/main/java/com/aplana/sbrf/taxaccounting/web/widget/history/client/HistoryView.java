package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.PopupPanel;
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
	private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");

	@UiField
	CellTable<LogBusiness> logsTable;

	@Inject
	public HistoryView(EventBus eventBus, Binder uiBinder) {
		super(eventBus);
		widget = uiBinder.createAndBindUi(this);
		initTable();
	}

	@Override
	public void setHistory(List<LogBusiness> logs, Map<Integer, String> userNames) {
		this.userNames = userNames;
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
				return FormDataEvent.getByCode(object.getEventId()).getTitle();
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
		logsTable.addColumn(noteColumn, "Текст события");

		logsTable.setWidth("1000px", false);
	}

}
