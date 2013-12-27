package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;

/**
 * Элемент дерева множественного выбора.
 */
public class MultiSelectTreeItem extends TreeItem implements HasClickHandlers,
        HasDoubleClickHandlers, HasMouseDownHandlers, HasValue<Boolean> {

    protected Integer id;
    protected boolean multiSelection;
    protected FocusPanel focusPanel;
    protected CheckBox checkBox;
    protected RadioButton radioButton;
    protected static final String RADIO_BUTTON_GROUP  = "MSI_GROUP";

    public MultiSelectTreeItem(Integer id, String name) {
        this(id, name, true);
    }

    public MultiSelectTreeItem(Integer id, String name, boolean multiSelection) {
        this.id = id;
        checkBox = new CheckBox(name);
        radioButton = new RadioButton(RADIO_BUTTON_GROUP, name);
        focusPanel = new FocusPanel();
        focusPanel.getElement().getStyle().setFontStyle(Style.FontStyle.NORMAL);
        setMultiSelection(multiSelection);
        setWidget(focusPanel);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name){
        ((CheckBox) getWidget()).setText(name);
    }

    public String getName() {
        return ((CheckBox) getWidget()).getText();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        getWidget().fireEvent(event);
    }

    @Override
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return ((CheckBox) getWidget()).addDoubleClickHandler(handler);
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return ((CheckBox) getWidget()).addMouseDownHandler(handler);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return ((CheckBox) getWidget()).addClickHandler(handler);
    }

    @Override
    public Widget getWidget() {
        return ((FocusPanel)super.getWidget()).getWidget();
    }

    public FocusPanel getPanel() {
        return focusPanel;
    }

    @Override
    public void addItem(TreeItem item) {
        super.addItem(item);
        // выравнивание смещения узлов дерева
        String lm = item.getElement().getStyle().getMarginLeft();
        if (lm != null && !"".equals(lm)) {
            lm = lm.replaceAll("px", "");
            double tmp = Double.valueOf(lm);
            if (tmp > 0.0) {
                item.getElement().getStyle().setMarginLeft(tmp + 7.0, Style.Unit.PX);
            }
        }
    }

    /** Установить чекбокс или радиоконопку. */
    public void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
        focusPanel.clear();
        if (multiSelection) {
            focusPanel.add(checkBox);
        } else {
            focusPanel.add(radioButton);
        }
    }

    public boolean isMultiSelection() {
        return multiSelection;
    }

    @Override
    public Boolean getValue() {
        return ((CheckBox) getWidget()).getValue();
    }

    @Override
    public void setValue(Boolean value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        checkBox.setValue(value, fireEvents);
        radioButton.setValue(value, fireEvents);
    }

    public void setValue(boolean value) {
        checkBox.setValue(value);
        radioButton.setValue(value);
    }

    public void setGroup(String name) {
        radioButton.setName(name);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        return ((CheckBox) getWidget()).addValueChangeHandler(handler);
    }
}
