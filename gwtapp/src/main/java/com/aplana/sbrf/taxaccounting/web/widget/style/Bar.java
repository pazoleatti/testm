package com.aplana.sbrf.taxaccounting.web.widget.style;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class Bar extends Composite implements HasWidgets {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, Bar> {
	}

	public Bar() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Panel leftPlaceHolder;

	@UiField
	Panel rightPlaceHolder;

	@Override
	public void add(Widget w) {
		if (w.getClass().equals(LeftBar.class)) {
			leftPlaceHolder.add(w);
		} else if (w.getClass().equals(RightBar.class)) {
			rightPlaceHolder.add(w);
		} else {
			throw new IllegalArgumentException(
					"Only LeftBar and RightBar are supported");
		}
	}

	@Override
	public void clear() {
		leftPlaceHolder.clear();
		rightPlaceHolder.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		List<Widget> list = new ArrayList<Widget>();
		for (Iterator<Widget> iterator = leftPlaceHolder.iterator(); iterator
				.hasNext();) {
			list.add(iterator.next());
		}
		for (Iterator<Widget> iterator = rightPlaceHolder.iterator(); iterator
				.hasNext();) {
			list.add(iterator.next());
		}
		return list.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return leftPlaceHolder.remove(w) || rightPlaceHolder.remove(w);
	}

}
