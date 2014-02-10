package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.RefBookFormDataKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация фабрики провайдеров данных для справочников
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:22
 */
@Service("refBookFactory")
@Transactional
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
    public List<RefBook> getAll(boolean onlyVisible, RefBookType type) {
        Integer typeId = type == null ? null : type.getId();
        return onlyVisible ? refBookDao.getAllVisible(typeId) : refBookDao.getAll(typeId);
    }

    @Override
    public RefBook getByAttribute(Long attributeId) {
        return refBookDao.getByAttribute(attributeId);
    }

    @Override
    public RefBookDataProvider getDataProvider(Long refBookId) {
        if (RefBookDepartment.REF_BOOK_ID.equals(refBookId)) {
            //return applicationContext.getBean("refBookDepartment", RefBookDataProvider.class);
            RefBookSimpleReadOnly refBookSimple =  (RefBookSimpleReadOnly) applicationContext.getBean("refBookSimpleReadOnly", RefBookDataProvider.class);
            refBookSimple.setRefBookId(RefBookSimpleReadOnly.DEPARTMENT_REF_BOOK_ID);
            refBookSimple.setTableName(RefBookSimpleReadOnly.DEPARTMENT_TABLE_NAME);
            return refBookSimple;
        } else if (RefBookIncome101.REF_BOOK_ID.equals(refBookId)) {
			return applicationContext.getBean("refBookIncome101", RefBookDataProvider.class);
        } else if (RefBookIncome102.REF_BOOK_ID.equals(refBookId)) {
			return applicationContext.getBean("refBookIncome102", RefBookDataProvider.class);
        } else if (RefBookUser.REF_BOOK_ID.equals(refBookId)) {
			return applicationContext.getBean("refBookUser", RefBookDataProvider.class);
        } else if (RefBookSimpleReadOnly.FORM_TYPE_REF_BOOK_ID.equals(refBookId)) { // Справочник "Виды налоговых форм"
			RefBookSimpleReadOnly refBookSimple =  (RefBookSimpleReadOnly) applicationContext.getBean("refBookSimpleReadOnly", RefBookDataProvider.class);
			refBookSimple.setRefBookId(RefBookSimpleReadOnly.FORM_TYPE_REF_BOOK_ID);
			refBookSimple.setTableName(RefBookSimpleReadOnly.FORM_TYPE_TABLE_NAME);
			refBookSimple.setWhereClause("STATUS = 0");
			return refBookSimple;
		} else if (RefBookSimpleReadOnly.SEC_ROLE_REF_BOOK_ID.equals(refBookId)) { // Справочник "Системные роли"
			RefBookSimpleReadOnly refBookSimple =  (RefBookSimpleReadOnly) applicationContext.getBean("refBookSimpleReadOnly", RefBookDataProvider.class);
			refBookSimple.setRefBookId(RefBookSimpleReadOnly.SEC_ROLE_REF_BOOK_ID);
			refBookSimple.setTableName(RefBookSimpleReadOnly.SEC_ROLE_TABLE_NAME);
			return refBookSimple;
        } else if(RefBookSimpleReadOnly.OKTMO_REF_BOOK_ID.equals(refBookId)) {  //  Справочник "ОКТМО"
            RefBookBigDataProvider dataProvider = (RefBookBigDataProvider) applicationContext.getBean("RefBookBigDataProvider", RefBookDataProvider.class);
            dataProvider.setRefBookId(refBookId);
            if (RefBookSimpleReadOnly.OKTMO_REF_BOOK_ID.equals(refBookId)) {
                dataProvider.setTableName(RefBookSimpleReadOnly.OKTMO_TABLE_NAME);
            }
            return dataProvider;
		} else if (RefBookFormDataKind.REF_BOOK_ID.equals(refBookId)) {
            return (RefBookFormDataKind) applicationContext.getBean("refBookFormDataKind", RefBookFormDataKind.class);
        } else{
			RefBookUniversal refBookUniversal = (RefBookUniversal) applicationContext.getBean("refBookUniversal", RefBookDataProvider.class);
			refBookUniversal.setRefBookId(refBookId);
			return refBookUniversal;
        }
    }

}
