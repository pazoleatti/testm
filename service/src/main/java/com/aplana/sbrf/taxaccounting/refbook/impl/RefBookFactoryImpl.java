package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.impl.RefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Реализация фабрики провайдеров данных для справочников
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:22
 */
@Service
public class RefBookFactoryImpl implements RefBookFactory {

	@Autowired
	private RefBookDao refBookDao;

	@Autowired
	private RefBookDataProvider refBookDataProvider;

	@Override
	public RefBook get(Long refBookId) {
		return refBookDao.get(refBookId);
	}

	@Override
	public List<RefBook> getAll() {
		return refBookDao.getAll();
	}

	@Override
	public RefBookDataProvider getDataProvider(long refBookId) {
		//здесь добавлять условия для учета нестандартных справочников
		return refBookDataProvider;
	}
}
