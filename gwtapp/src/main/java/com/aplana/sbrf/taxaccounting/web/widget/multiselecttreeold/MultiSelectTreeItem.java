package com.aplana.sbrf.taxaccounting.web.widget.multiselecttreeold;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * Элемент дерева множественного выбора.
 */
public class MultiSelectTreeItem extends TreeItem implements HasClickHandlers,
        HasDoubleClickHandlers, HasMouseDownHandlers {

    private Integer id;

    public MultiSelectTreeItem(Integer id, String name) {
        this.id = id;
        Label label = new Label(name);
//        FocusPanel focusPanel = new FocusPanel(label);
//        focusPanel.setFocus(true);
//        setWidget(focusPanel);
        setWidget(label);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setValue(String value){
        ((Label) getWidget()).setText(value);
    }

    public String getName() {
        return ((Label) getWidget()).getText();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        getWidget().fireEvent(event);
    }

    @Override
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return ((Label) getWidget()).addDoubleClickHandler(handler);
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return ((Label) getWidget()).addMouseDownHandler(handler);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return ((Label) getWidget()).addClickHandler(handler);
    }

//    @Override
//    public Widget getWidget() {
//        return ((SimplePanel)super.getWidget()).getWidget();
//    }
}
