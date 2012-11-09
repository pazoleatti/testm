package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class DictionaryDataProvider extends AsyncDataProvider<SimpleDictionaryItem<Long>>{

	@Override
	protected void onRangeChanged(HasData<SimpleDictionaryItem<Long>> display) {
	      final Range range = display.getVisibleRange();

	      new Timer() {
	        @Override
	        public void run() {
	          int start = range.getStart();
	          int length = range.getLength();
	          List<SimpleDictionaryItem<Long>> newData = new ArrayList<SimpleDictionaryItem<Long>>();
	          for (int i = start; i < start + length; i++) {
	        	  SimpleDictionaryItem<Long> item = new SimpleDictionaryItem<Long>();
	        	  item.setName("Name " + String.valueOf(i));
	        	  item.setValue(new Long(i));
	        	  newData.add(item);
	          }

	          updateRowData(start, newData);
	        }
	      }.schedule(3000);		
	}

}
