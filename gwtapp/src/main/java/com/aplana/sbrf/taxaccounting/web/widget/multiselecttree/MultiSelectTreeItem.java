package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * Элемент дерева множественного выбора.
 */
public class MultiSelectTreeItem extends TreeItem implements HasClickHandlers,
        HasDoubleClickHandlers, HasMouseDownHandlers {

    private Integer id;
    private FocusPanel focusPanel;

    public MultiSelectTreeItem(Integer id, String name) {
        this.id = id;
        CheckBox widget = new CheckBox(name);
        focusPanel = new FocusPanel(widget);
        setWidget(focusPanel);
        // setWidget(widget);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setValue(String value){
        ((CheckBox) getWidget()).setText(value);
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
                item.getElement().getStyle().setMarginLeft(tmp + 5.0, Style.Unit.PX);
            }
        }
    }
}
