package com.aplana.sbrf.taxaccounting.web.widget.style;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class LeftBar extends Composite implements HasWidgets{

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, LeftBar> {
	}
	
	@UiField
	Panel placeHolder;

	public LeftBar() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void add(Widget w) {
		w.setStyleName("buttonWithMargin", true);
		placeHolder.add(w);
	}

	@Override
	public void clear() {
		placeHolder.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return placeHolder.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return placeHolder.remove(w);
	}

}
