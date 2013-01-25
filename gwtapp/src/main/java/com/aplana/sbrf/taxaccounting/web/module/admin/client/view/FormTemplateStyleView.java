package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;

import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateStylePresenter;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class FormTemplateStyleView extends ViewWithUiHandlers<FormTemplateStyleUiHandlers>
		implements FormTemplateStylePresenter.MyView, Editor<FormStyle> {

	interface Binder extends UiBinder<Widget, FormTemplateStyleView> { }

	interface MyDriver extends SimpleBeanEditorDriver<FormStyle, FormTemplateStyleView> {
	}

	private final Widget widget;
	private final MyDriver driver;
	private List<FormStyle> styles;
	private static final List<Color> colorTitles = new ArrayList<Color>();

	@UiField
	ListBox styleListBox;

	@UiField
	CheckBox italic;

	@UiField
	CheckBox bold;

	@UiField
	Button addStyle;

	@UiField
	Button removeStyle;

	@UiField
	TextBox alias;

	@UiField(provided = true)
	ValueListBox<Color> fontColor;

	@UiField(provided = true)
	ValueListBox<Color> backColor;

	@Inject
	@UiConstructor
	public FormTemplateStyleView(final Binder uiBinder, final MyDriver driver) {
		Collections.addAll(colorTitles, Color.values());

		fontColor = new ValueListBox<Color>(new AbstractRenderer<Color>() {
			@Override
			public String render(Color color) {
				if (color == null) {
					return "";
				}
				return color.getTitle();
			}
		});
		fontColor.setAcceptableValues(colorTitles);

		backColor = new ValueListBox<Color>(new AbstractRenderer<Color>() {
			@Override
			public String render(Color color) {
				if (color == null) {
					return "";
				}
				return color.getTitle();
			}
		});
		backColor.setAcceptableValues(colorTitles);

		widget = uiBinder.createAndBindUi(this);
		this.driver = driver;
		this.driver.initialize(this);
	}

	@Override
	public void setViewData(List<FormStyle> styles) {
		if (styles != null) {
			this.styles = styles;
			if (styles.size() > 0) {
				setupStyles(0);
			}
		}
	}

	@Override
	public void onFlush() {
		flush();
	}

	private void setupStyles(int index) {
		setStyleList();
		setStyleParams(index);
		styleListBox.setSelectedIndex(index);
	}

	private void setStyleList() {
		if (styles != null) {
			styleListBox.clear();
			for (FormStyle style : styles) {
				styleListBox.addItem(style.getAlias(), String.valueOf(styles.indexOf(style)));
			}
		}
	}

	@UiHandler("styleListBox")
	public void onSelectColumn(ChangeEvent event){
		flush();
		setStyleParams(styleListBox.getSelectedIndex());
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	private void setStyleParams(int index) {
		driver.edit(styles.get(index));
	}

	@UiHandler("addStyle")
	public void onAddStyle(ClickEvent event){
		FormStyle newStyle = new FormStyle();
		newStyle.setAlias("Новый");
		newStyle.setBackColor(Color.WHITE);
		newStyle.setFontColor(Color.WHITE);
		styles.add(newStyle);
		setupStyles(styles.size() - 1);
	}

	@UiHandler("removeStyle")
	public void onRemoveStyle(ClickEvent event){
		int index = styleListBox.getSelectedIndex();
 		styles.remove(index);
		if (index > 0) {
			setupStyles(index - 1);
		}
		else {
			setupStyles(0);
		}
	}

	@UiHandler("alias")
	public void onKeyUpAlias(KeyUpEvent event){
		flush();
		setupStyles(styleListBox.getSelectedIndex());
	}

	@UiHandler("alias")
	public void onChangeAlias(ChangeEvent event){
		flush();
		setupStyles(styleListBox.getSelectedIndex());
	}

	private void flush() {
		driver.flush();
	}

}