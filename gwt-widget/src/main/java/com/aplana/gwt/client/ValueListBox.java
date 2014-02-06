package com.aplana.gwt.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.HasConstrainedValue;

import java.util.Collection;

/**
 * ValueListBox<T>, который в задимсабленном состоянии показывает Label.
 *
 * @author  fmukhametdinov
 */
public class ValueListBox<T> extends DoubleStateWrapper<com.google.gwt.user.client.ui.ValueListBox<T>, T> implements HasConstrainedValue<T>, HasValueChangeHandlers<T> {

    public ValueListBox(Renderer<T> renderer) {
        super(new com.google.gwt.user.client.ui.ValueListBox(renderer));
    }

    public void setAcceptableValues(Collection<T> newValues) {
        widget.setAcceptableValues(newValues);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return widget.addValueChangeHandler(handler);
    }
}
