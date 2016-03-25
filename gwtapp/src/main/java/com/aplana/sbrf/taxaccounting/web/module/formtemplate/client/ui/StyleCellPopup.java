package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateRowView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.Arrays;
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

    @UiField(provided = true)
    ValueListBox<Color> fontColor;

    @UiField(provided = true)
    ValueListBox<Color> backColor;

	@UiField
	Button saveButton;

	@UiField
	CheckBox editable, italic, bold;

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

        styleAlias.addHandler(new ValueChangeHandler<FormStyle>() {
            @Override
            public void onValueChange(ValueChangeEvent<FormStyle> event) {
                setStyle(event.getValue());
            }
        }, ValueChangeEvent.getType());

        fontColor = new ValueListBox<Color>(new AbstractRenderer<Color>() {
            @Override
            public String render(Color object) {
                if (object == null) {
                    return "";
                }
                return object.getTitle();
            }
        });
        fontColor.addHandler(new ValueChangeHandler<Color>() {
            @Override
            public void onValueChange(ValueChangeEvent<Color> event) {
                styleAlias.setValue(null);
            }
        }, ValueChangeEvent.getType());

        backColor = new ValueListBox<Color>(new AbstractRenderer<Color>() {
            @Override
            public String render(Color object) {
                if (object == null) {
                    return "";
                }
                return object.getTitle();
            }
        });
        backColor.addHandler(new ValueChangeHandler<Color>() {
            @Override
            public void onValueChange(ValueChangeEvent<Color> event) {
                styleAlias.setValue(null);
            }
        }, ValueChangeEvent.getType());

        fontColor.setAcceptableValues(Arrays.asList(Color.values()));
        backColor.setAcceptableValues(Arrays.asList(Color.values()));

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

    @UiHandler("italic")
    void onItalicValueChange(ValueChangeEvent<Boolean> event) {
        styleAlias.setValue(null);
    }

    @UiHandler("bold")
    void onBoldValueChange(ValueChangeEvent<Boolean> event) {
        styleAlias.setValue(null);
    }

    private void setStyle(FormStyle formStyle) {
        if (formStyle != null) {
            fontColor.setValue(formStyle.getFontColor());
            backColor.setValue(formStyle.getBackColor());
            italic.setValue(formStyle.isItalic());
            bold.setValue(formStyle.isBold());
        }
    }

    private FormStyle getStyle() {
        return new FormStyle(null, fontColor.getValue(), backColor.getValue(), italic.getValue(), bold.getValue());
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
				styleAlias.setValue(null);
                setStyle(cells.get(0).getStyle());
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
    		cell.setStyle(getStyle());
        }
		parent.refresh();
		popup.hide();
	}
}