package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
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

		List<DictionaryItem<T>> items;
		if (action.getSearchPattern() != null && !action.getSearchPattern().isEmpty()) {
			items = dictionaryDataProvider.getValues(action.getSearchPattern());
		} else {
			items = dictionaryDataProvider.getValues();
		}

		DictionaryResult<T> result = new DictionaryResult<T>();
		int begin = action.getOffset();
		int end = action.getOffset() + action.getMax();
		int size = items.size();
		if (begin < size && (begin > 0 || end < size)) {
			end = Math.min(end, size);
			result.setDictionaryItems(new ArrayList<DictionaryItem<T>>(items.subList(begin, end)));
		} else if (begin > size) {
			result.setDictionaryItems(new ArrayList<DictionaryItem<T>>(0));
		} else {
			result.setDictionaryItems(items);
		}
		result.setSize(size);
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
