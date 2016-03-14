package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateRowView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.List;


public class StyleCellPopup extends Composite {

	interface StyleWidgetUiBinder extends UiBinder<HTMLPanel, StyleCellPopup> {
	}

	private static StyleWidgetUiBinder ourUiBinder = GWT.create(StyleWidgetUiBinder.class);
	private PopupPanel popup = new PopupPanel();
	private List<Cell> cells;
	private final FormTemplateRowView parent;

	@UiField
	Label title;

	@UiField(provided = true)
	ValueListBox<FormStyle> styleAlias;

	@UiField
	Button saveButton;

	@UiField
	CheckBox editable;

	@UiConstructor
	public StyleCellPopup(FormTemplateRowView parent) {
		super();
		this.parent = parent;

		styleAlias = new ValueListBox<FormStyle>(new AbstractRenderer<FormStyle>() {
			@Override
			public String render(FormStyle object) {
				if (object == null) {
					return "";
				}
				return object.getAlias();
			}
		});

		initWidget(ourUiBinder.createAndBindUi(this));

		this.addDomHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					save();
				}
			}
		}, KeyPressEvent.getType());

		popup.setWidget(this);
		popup.setAnimationEnabled(true);
		popup.setAutoHideEnabled(true);
	}

	public void setValue(List<Cell> cells) {
		this.cells = cells;
		if (cells.size() == 1) {
			title.setText("Стиль ячейки");
		} else {
			title.setText("Стиль ячеек");
		}
	}

	@UiHandler("saveButton")
	public void onClickSaveButton(ClickEvent event) {
		save();
	}

	public void show(int left, int top) {
		if (!cells.isEmpty()) {
			editable.setValue(false);
			styleAlias.setValue(null);

			if (cells.size() == 1) {
				editable.setValue(cells.get(0).isEditable());
				styleAlias.setValue(cells.get(0).getStyle());
			}

			popup.setPopupPosition(left, top);
			popup.show();
		}
	}

	public void setStyleAlias(List<FormStyle> styleAliases) {
		this.styleAlias.setAcceptableValues(styleAliases);
	}

	private void save() {
		for (Cell cell : cells) {
			cell.setEditable(editable.getValue());
			if (styleAlias.getValue() != null) {
				cell.setStyle(styleAlias.getValue());
			} else {
				cell.setStyle(null);
			}
		}
		parent.refresh();
		popup.hide();
	}
}