package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.DictionaryResult;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public abstract class DictionaryHandler<A extends DictionaryAction<R, T>, R extends DictionaryResult<T>, T extends Serializable>
		extends AbstractActionHandler<A, R> {
	private final Log log = LogFactory.getLog(DictionaryHandler.class);

	public DictionaryHandler(Class<A> actionType) {
		super(actionType);
	}

	@Override
	public R execute(A action, ExecutionContext context) throws ActionException {
		try {
			List<DictionaryItem<T>> items = selectDictionaryItems(action);
			R result = createResult();
			if ((action.getOffset() + action.getMax()) <= items.size()) {
				result.setDictionaryItems(new ArrayList<DictionaryItem<T>>(items.subList(action.getOffset(), action.getOffset() + action.getMax())));
				result.setSize(items.size());
			} else {
				result.setDictionaryItems(items);
				result.setSize(items.size());
			}
			return result;
		} catch (Throwable e) {
			log.error("Error!", e);
			throw new ActionException(e);
		}
	}

	protected abstract R createResult();

	protected abstract List<DictionaryItem<T>> selectDictionaryItems(A action);

	@Override
	public void undo(A action, R result, ExecutionContext context) throws ActionException {
	}
}
