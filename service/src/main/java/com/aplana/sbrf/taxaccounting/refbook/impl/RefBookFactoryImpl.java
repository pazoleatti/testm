package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Реализация фабрики провайдеров данных для справочников
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:22
 */
@Service("refBookFactory")
public class RefBookFactoryImpl implements RefBookFactory {

	@Autowired
	private RefBookDao refBookDao;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public RefBook get(Long refBookId) {
		return refBookDao.get(refBookId);
	}

	@Override
	public List<RefBook> getAll() {
		return refBookDao.getAll();
	}

	@Override
	public RefBook getByAttribute(Long attributeId) {
		return refBookDao.getByAttribute(attributeId);
	}

	@Override
	public RefBookDataProvider getDataProvider(Long refBookId) {
		RefBookDataProvider refBookDataProvider = applicationContext.getBean("universal", RefBookDataProvider.class);   // Исправление Марата, надо сделать получать данные отдельно для конкретных провайдеров
		if (refBookDataProvider instanceof RefBookUniversal) {
			((RefBookUniversal) refBookDataProvider).setRefBookId(refBookId);
		}
		//здесь добавлять условия для учета нестандартных справочников
		return refBookDataProvider;
	}
}
