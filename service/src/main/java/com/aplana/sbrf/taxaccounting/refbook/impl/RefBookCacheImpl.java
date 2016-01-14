package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookCache;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Локальный кэш для работы со справочниками с коротким временем жизни. Живет только во время выполнения операций
 * со справочниками. Частично заменяет кэш на уровне сервера приложений.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 14.01.2016 18:28
 */

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RefBookCacheImpl implements RefBookCache {

	@Autowired
	RefBookFactory refBookFactory;

	private Map<Long, RefBook> refBooks = new HashMap<Long, RefBook>();
	private Map<Long, RefBook> refBookByAttrs = new HashMap<Long, RefBook>();
	private Map<Long, RefBookDataProvider> dataProviders = new HashMap<Long, RefBookDataProvider>();

	@Override
	public RefBook get(Long refBookId) {
		RefBook refBook = refBooks.get(refBookId);
		if (refBook == null) {
			refBook = refBookFactory.get(refBookId);
			refBooks.put(refBookId, refBook);
			// сразу заполняем по всем атрибутам
			for (RefBookAttribute attr : refBook.getAttributes()) {
				refBookByAttrs.put(attr.getId(), refBook);
			}
		}
		return refBook;
	}

	@Override
	public RefBook getByAttribute(Long attributeId) {
		RefBook refBook = refBookByAttrs.get(attributeId);
		if (refBook == null) {
			refBook = refBookFactory.getByAttribute(attributeId);
			// сразу заполняем по всем атрибутам
			for (RefBookAttribute attr : refBook.getAttributes()) {
				refBookByAttrs.put(attr.getId(), refBook);
			}
		}
		return refBook;
	}

	@Override
	public RefBookDataProvider getDataProvider(Long refBookId) {
		RefBookDataProvider provider = dataProviders.get(refBookId);
		if (provider == null) {
			provider = refBookFactory.getDataProvider(refBookId);
			dataProviders.put(refBookId, provider);
		}
		return provider;
	}
}