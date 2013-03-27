package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ValueListBox;

import java.util.Collection;

/**
 * Выпадающий список с возможностью отображать значения
 * в виде подсказки для элементов(это необходимо для длинных значений в IE8)
 */
public class ListBoxWithTooltip<T> extends ValueListBox<T> {
	boolean showTooltip = false;

	Renderer<T> renderer;

	public boolean isShowTooltip() {
		return showTooltip;
	}

	public void setShowTooltip(boolean showTooltip) {
		this.showTooltip = showTooltip;
	}

	public ListBoxWithTooltip(Renderer<T> renderer) {
		super(renderer);
		this.renderer = renderer;
	}

	@Override
	public void setAcceptableValues(Collection<T> newValues) {
		super.setAcceptableValues(newValues);
		if (showTooltip) {
			setTitle(renderer.render(getValue()));
			this.addValueChangeHandler(new ValueChangeHandler<T>() {
				@Override
				public void onValueChange(ValueChangeEvent<T> event) {
					ListBoxWithTooltip.this.setTitle(renderer.render(getValue()));
				}
			});
			if (Window.Navigator.getUserAgent().contains("MSIE 8")) {
				int i = 0;
				for (T value : newValues) {
					Node child = this.getElement().getChild(i++);
					Element.as(child).setTitle(renderer.render(value));
				}
			}
		}
	}
}
