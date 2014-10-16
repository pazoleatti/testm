package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import com.aplana.sbrf.taxaccounting.web.widget.ui.HasHighlighting;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

/**
 * Элемент дерева множественного выбора.
 *
 * @author rtimerbaev
 */
public class MultiSelectTreeItem extends TreeItem implements HasClickHandlers,
        HasDoubleClickHandlers, HasMouseDownHandlers, HasValue<Boolean>, HasHighlighting {

    protected Integer id;
    /** Тип узла дерева: true - с чекбоксом, false - с радиокнопкой, null - только с текстом*/
    protected Boolean multiSelection;

    protected Label label;
    protected CheckBox checkBox;
    protected RadioButton radioButton;
    protected static final String RADIO_BUTTON_GROUP  = "MSI_GROUP";

    public MultiSelectTreeItem(String name) {
        this(null, name, null);
    }

    /** Элемент дерева множественного выбора. По-умолчанию создается узел с чекбоксом. */
    public MultiSelectTreeItem(Integer id, String name) {
        this(id, name, true);
    }

    public MultiSelectTreeItem(String name, Boolean multiSelection) {
        this(null, name, multiSelection);
    }

    public MultiSelectTreeItem(Integer id, String name, Boolean multiSelection) {
        this.id = id;
        label = new Label(name);
        checkBox = new CheckBox(name);
        radioButton = new RadioButton(RADIO_BUTTON_GROUP, name);

        setMultiSelection(multiSelection);
        setWidget(multiSelection == null ? label : multiSelection ? checkBox : radioButton);

        // установка стилей отображения элемента
        label.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        checkBox.getElement().getStyle().setWidth(100, Style.Unit.PCT);
        checkBox.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        checkBox.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        radioButton.getElement().getStyle().setWidth(100, Style.Unit.PCT);
        radioButton.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        radioButton.getElement().getStyle().setDisplay(Style.Display.BLOCK);

        Style checkboxInputStyle = DOM.getChild(checkBox.getElement(), 0).getStyle();
        Style checkboxLabelStyle = DOM.getChild(checkBox.getElement(), 1).getStyle();
        Style radioInputStyle = DOM.getChild(radioButton.getElement(), 0).getStyle();
        Style radioLabelStyle = DOM.getChild(radioButton.getElement(), 1).getStyle();

        checkboxInputStyle.setCursor(Style.Cursor.POINTER);
        checkboxLabelStyle.setWidth(100, Style.Unit.PCT);
        checkboxLabelStyle.setDisplay(Style.Display.INLINE_BLOCK);
        checkboxLabelStyle.setCursor(Style.Cursor.POINTER);
        // хак для одинокого итема так как там ширина в 100%
        checkboxLabelStyle.setPaddingRight(15, Style.Unit.PX);
        checkboxLabelStyle.setMarginRight(-34, Style.Unit.PX);

        radioInputStyle.setCursor(Style.Cursor.POINTER);
        radioLabelStyle.setWidth(100, Style.Unit.PCT);
        radioLabelStyle.setDisplay(Style.Display.INLINE_BLOCK);
        radioLabelStyle.setCursor(Style.Cursor.POINTER);
        // хак для одинокого итема
        radioLabelStyle.setPaddingRight(15, Style.Unit.PX);
        radioLabelStyle.setMarginRight(-34, Style.Unit.PX);


        // усчтановка хендлеров
        label.addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                MultiSelectTreeItem.this.setState(!MultiSelectTreeItem.this.getState());
            }
        });
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MultiSelectTreeItem.this.setState(!MultiSelectTreeItem.this.getState());
            }
        });
    }

    @Override
    public void highLightText(String textToHighLight) {
        if(textToHighLight!= null && !textToHighLight.isEmpty()) {
            String highLightedString = RegExp.compile(textToHighLight, "gi").replace(getText(), "<span style=\"color: #ff0000;\">$&</span>");
            label.getElement().setInnerHTML(highLightedString);
            DOM.getChild(checkBox.getElement(), 1).setInnerHTML(highLightedString);
            DOM.getChild(radioButton.getElement(), 1).setInnerHTML(highLightedString);
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public void setText(String text) {
        label.setText(text);
        checkBox.setText(text);
        radioButton.setText(text);
    }

    @Override
    public String getText() {
        if (multiSelection == null) {
            return label.getText();
        } else {
            return ((CheckBox) getWidget()).getText();
        }
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        getWidget().fireEvent(event);
    }

    @Override
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return ((HasDoubleClickHandlers) getWidget()).addDoubleClickHandler(handler);
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return ((HasMouseDownHandlers) getWidget()).addMouseDownHandler(handler);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return ((HasClickHandlers) getWidget()).addClickHandler(handler);
    }

    /** Установить чекбокс или радиокнопку. */
    public final void setMultiSelection(Boolean multiSelection) {
        this.multiSelection = multiSelection;
        setWidget(multiSelection == null ? label : multiSelection ? checkBox : radioButton);
    }

    public Boolean isMultiSelection() {
        return multiSelection;
    }

    @Override
    public Boolean getValue() {
        return (multiSelection == null ? false : ((CheckBox) getWidget()).getValue());
    }

    @Override
    public void setValue(Boolean value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        checkBox.setValue(multiSelection == null ? false : value, fireEvents);
        radioButton.setValue(multiSelection == null ? false : value, fireEvents);
    }

    public void setGroup(String name) {
        radioButton.setName(name);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        if (multiSelection == null) {
            return null;
        }
        return ((CheckBox) getWidget()).addValueChangeHandler(handler);
    }
}
