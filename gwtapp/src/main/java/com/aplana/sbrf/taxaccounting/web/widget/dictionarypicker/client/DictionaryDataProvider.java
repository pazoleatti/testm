package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.entry.client.ClientGinjector;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryPickerDataAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryPickerDataResult;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

import java.io.Serializable;
import java.util.List;

/**
 * Асинхронный провайдер для таблицы, получающий даные с сервера 
 * с использованием фильтра и кода справочника.
 * @author Eugene Stetsenko
 */
public class DictionaryDataProvider<ValueType extends Serializable> extends AsyncDataProvider<DictionaryItem<ValueType>>{
	
	private String filter = "%";
	private final DispatchAsync dispatcher;
	private String dictionaryCode;
	
	/**
	 * 
	 * @param newFilter Фильтр.
	 * @param dictionaryCode Код справочника.
	 */
	public DictionaryDataProvider(String newFilter, String dictionaryCode) {
		filter = newFilter;
		dispatcher = ((ClientGinjector)DelayedBindRegistry.getGinjector()).getDispatchAsync();
		this.dictionaryCode = dictionaryCode; 
	}

	@Override
	protected void onRangeChanged(final HasData<DictionaryItem<ValueType>> display) {
	      final Range range = display.getVisibleRange();
          final int start = range.getStart();
          final int length = range.getLength();
          
          DictionaryPickerDataAction action = new DictionaryPickerDataAction();
          if (!filter.isEmpty()) {
        	  action.setFilter(filter);
          } else {
        	  action.setFilter("%");
          }
          action.setStart(start);
          action.setOffset(length);
          action.setDictionaryCode(dictionaryCode);
          dispatcher.execute(action, new AbstractCallback<DictionaryPickerDataResult>() {
  			@Override
  			public void onReqSuccess(DictionaryPickerDataResult result) {
  				updateRowData(start, (List) result.getDictionaryItems());
  				display.setRowCount(result.getSize());
  			}
  		});
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	private void find() {
		
	}

}
