package com.aplana.sbrf.taxaccounting.web.widget.tools;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class Bar extends Composite implements HasWidgets {

	private static BarUiBinder uiBinder = GWT.create(BarUiBinder.class);

	interface BarUiBinder extends UiBinder<Widget, Bar> {
	}

	public Bar() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Panel leftPlaceHolder;
	
	Panel rightPlaceHolder;

	public Bar(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void add(Widget w) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<Widget> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Widget w) {
		
		// TODO Auto-generated method stub
		return false;
	}

}
