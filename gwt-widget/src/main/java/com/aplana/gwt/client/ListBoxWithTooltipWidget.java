package com.aplana.gwt.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;

import java.util.Collection;

/**
 * @author fmukhametdinov
 */
public class ListBoxWithTooltipWidget<T> extends DoubleStateWrapper<ListBoxWithTooltip<T>, T> implements HasValueChangeHandlers<T> {

    public ListBoxWithTooltipWidget(Renderer<T> renderer) {
        super(new ListBoxWithTooltip<T>(renderer));
    }

    public void setAcceptableValues(Collection<T> newValues) {
        widget.setAcceptableValues(newValues);
    }

    public void setShowTooltip(boolean showTooltip) {
        widget.setShowTooltip(showTooltip);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return widget.addValueChangeHandler(handler);
    }

}
