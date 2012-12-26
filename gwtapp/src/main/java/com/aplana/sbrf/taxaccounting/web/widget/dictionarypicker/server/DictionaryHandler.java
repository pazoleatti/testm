package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Базовый класс хандлера для получения значений справочника.
 *
 * @param <A> действие
 * @param <T> тип значения справочника
 * @author Vitalii Samolovskikh
 */
public abstract class DictionaryHandler<A extends DictionaryAction<T>, T extends Serializable>
		extends AbstractActionHandler<A, DictionaryResult<T>> {
	@SuppressWarnings("UnusedDeclaration")
	private final Log log = LogFactory.getLog(DictionaryHandler.class);

	public DictionaryHandler(Class<A> actionType) {
		super(actionType);
	}

	@Override
	public DictionaryResult<T> execute(A action, ExecutionContext context) throws ActionException {
		DictionaryDataProvider<T> dictionaryDataProvider = getDictionaryDataProvider(action.getDictionaryCode());
		PaginatedSearchResult<DictionaryItem<T>> items = dictionaryDataProvider.getValues(
					action.getSearchPattern()!=null ? action.getSearchPattern() : "",
					new PaginatedSearchParams(action.getOffset(), action.getMax())
		);

		DictionaryResult<T> result = new DictionaryResult<T>();
		long begin = action.getOffset();
		long size = items.getTotalRecordCount();
		if (begin > size) {
			result.setDictionaryItems(new ArrayList<DictionaryItem<T>>(0));
		} else {
			result.setDictionaryItems(items.getRecords());
		}
		result.setSize(items.getTotalRecordCount());

		return result;
	}

	/**
	 * Источники данных для справочников различных типов разные. Поэтому, получение источника данных для справчоника
	 * осуществляется в дочерних классах.
	 *
	 * @param dictionaryCode код справочника
	 * @return источник данных справочника
	 */
	protected abstract DictionaryDataProvider<T> getDictionaryDataProvider(String dictionaryCode);

	@Override
	public void undo(A action, DictionaryResult<T> result, ExecutionContext context) throws ActionException {
	}
}
