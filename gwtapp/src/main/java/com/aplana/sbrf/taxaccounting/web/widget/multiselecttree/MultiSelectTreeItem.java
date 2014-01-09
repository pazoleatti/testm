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
    /** Тип узла дерева: true - с чекбоксом, false - с радиокнопкой, null - только с текстом*/
    protected Boolean multiSelection;
    protected FocusPanel focusPanel;
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
        focusPanel = new FocusPanel();
//        focusPanel.getElement().getStyle().setFontStyle(Style.FontStyle.NORMAL); // TODO (Ramil Timerbaev)
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
        label.setText(name);
        checkBox.setText(name);
        radioButton.setText(name);
    }

    public String getName() {
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

    /** Установить чекбокс или радиокнопку. */
    public void setMultiSelection(Boolean multiSelection) {
        this.multiSelection = multiSelection;
        focusPanel.clear();
        if (multiSelection == null) {
            focusPanel.add(label);
        } else if (multiSelection) {
            focusPanel.add(checkBox);
        } else {
            focusPanel.add(radioButton);
        }
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
        if (multiSelection == null) {
            checkBox.setValue(false, fireEvents);
            radioButton.setValue(false, fireEvents);
        } else {
            checkBox.setValue(value, fireEvents);
            radioButton.setValue(value, fireEvents);
        }
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
