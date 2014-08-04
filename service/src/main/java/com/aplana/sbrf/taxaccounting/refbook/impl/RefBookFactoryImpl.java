package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.RefBookDepartmentType;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.RefBookConfigurationParam;
import com.aplana.sbrf.taxaccounting.refbook.impl.fixed.RefBookAuditFieldList;
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
    public List<RefBook> getAll(boolean onlyVisible) {
		//TODO: избавиться от лишнего аргумента null (Marat Fayzullin 10.02.2014)
        return onlyVisible ? refBookDao.getAllVisible(null) : refBookDao.getAll(null);
    }

    @Override
    public RefBook getByAttribute(Long attributeId) {
        return refBookDao.getByAttribute(attributeId);
    }

    @Override
    public RefBookDataProvider getDataProvider(Long refBookId) {
        if (RefBookDepartment.REF_BOOK_ID.equals(refBookId)) {
            return applicationContext.getBean("refBookDepartment", RefBookDataProvider.class);
        } else if (RefBookIncome101.REF_BOOK_ID.equals(refBookId)) {
			return applicationContext.getBean("refBookIncome101", RefBookDataProvider.class);
        } else if (RefBookIncome102.REF_BOOK_ID.equals(refBookId)) {
			return applicationContext.getBean("refBookIncome102", RefBookDataProvider.class);
        } else if (RefBookUser.REF_BOOK_ID.equals(refBookId)) {
            RefBookSimpleReadOnly refBookSimple =  (RefBookSimpleReadOnly) applicationContext.getBean("refBookSimpleReadOnly", RefBookDataProvider.class);
            refBookSimple.setRefBookId(RefBookSimpleReadOnly.USER_REF_BOOK_ID);
            refBookSimple.setTableName(RefBookSimpleReadOnly.USER_TABLE_NAME);
            return refBookSimple;
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
        } else if(RefBookOktmoProvider.OKTMO_REF_BOOK_ID.equals(refBookId)) {  //  Справочник "ОКТМО"
            RefBookOktmoProvider dataProvider = (RefBookOktmoProvider) applicationContext.getBean("RefBookOktmoProvider", RefBookDataProvider.class);
            dataProvider.setRefBookId(refBookId);
            if (RefBookOktmoProvider.OKTMO_REF_BOOK_ID.equals(refBookId)) {
                dataProvider.setTableName(RefBookOktmoProvider.OKTMO_TABLE_NAME);
            }
            return dataProvider;
		} else if (RefBookFormDataKind.REF_BOOK_ID.equals(refBookId)) { // Справавочник "Типы налоговых форм"
            RefBookFormDataKind dataProvider = applicationContext.getBean("refBookFormDataKind", RefBookFormDataKind.class);
            dataProvider.setRefBookId(refBookId);
            return dataProvider;
		} else if (RefBookDepartmentType.REF_BOOK_ID.equals(refBookId)) { // Справочник "Типы подразделений"
            RefBookDepartmentType dataProvider = applicationContext.getBean("refBookDepartmentType", RefBookDepartmentType.class);
            dataProvider.setRefBookId(refBookId);
            return dataProvider;
        } else if (RefBookConfigurationParam.REF_BOOK_ID.equals(refBookId)) {
            RefBookConfigurationParam dataProvider = applicationContext.getBean("refBookConfigurationParam", RefBookConfigurationParam.class);
            dataProvider.setRefBookId(refBookId);
            return dataProvider;
		}  else if (RefBookAuditFieldList.REF_BOOK_ID.equals(refBookId)) {
            RefBookAuditFieldList dataProvider = applicationContext.getBean("refBookAuditFieldList", RefBookAuditFieldList.class);
            dataProvider.setRefBookId(refBookId);
            return dataProvider;
		} else if (RefBookBookerStatementPeriod.REF_BOOK_ID.equals(refBookId)) {
            RefBookBookerStatementPeriod dataProvider = applicationContext.getBean("refBookBookerStatementPeriod", RefBookBookerStatementPeriod.class);
            dataProvider.setRefBookId(refBookId);
            return dataProvider;
        } else {
			RefBookUniversal refBookUniversal = (RefBookUniversal) applicationContext.getBean("refBookUniversal", RefBookDataProvider.class);
			refBookUniversal.setRefBookId(refBookId);
			return refBookUniversal;
        }
    }

    @Override
    public String getSearchQueryStatement(String query, Long refBookId) {
        if (query == null || query.isEmpty()){
            return null;
        }

        String q = query.trim().toLowerCase();
        StringBuilder resultSearch = new StringBuilder();
        RefBook refBook = get(refBookId);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (resultSearch.length() > 0){
                resultSearch.append(" or ");
            }

            switch (attribute.getAttributeType()) {
                case STRING:
                    resultSearch
                            .append("LOWER(")
                            .append(attribute.getAlias())
                            .append(")");
                    break;
                case NUMBER:
                    resultSearch
                            .append("TO_CHAR(")
                            .append(attribute.getAlias())
                            .append(")");
                    break;
                case DATE:
                    resultSearch.append(attribute.getAlias());
                    break;
                case REFERENCE:
                    if (isSimpleRefBool(refBookId)){
                        String fullAlias = getStackAlias(attribute);
                        switch (getLastAttribute(attribute).getAttributeType()){
                            case STRING:
                                resultSearch
                                        .append("LOWER(")
                                        .append(fullAlias)
                                        .append(")");
                                break;
                            case NUMBER:
                                resultSearch
                                        .append("TO_CHAR(")
                                        .append(fullAlias)
                                        .append(")");
                                break;
                            case DATE:
                                resultSearch.append(fullAlias);
                                break;
                            default:
                                throw new RuntimeException("Unknown RefBookAttributeType");
                        }
                    } else {
                        resultSearch
                                .append("TO_CHAR(")
                                .append(attribute.getAlias())
                                .append(")");
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown RefBookAttributeType");

            }

            resultSearch
                    .append(" like ")
                    .append("'%")
                    .append(q)
                    .append("%'");
        }

        return resultSearch.toString();
    }

    /**
     * Метод возврадет полный алиас для ссылочного атрибута вида
     * user.city.name
     *
     * @param attribute
     * @return
     */
    private String getStackAlias(RefBookAttribute attribute){
        switch (attribute.getAttributeType()) {
            case STRING:
            case DATE:
            case NUMBER:
                return attribute.getAlias();
            case REFERENCE:
                RefBook rb = get(attribute.getRefBookId());
                RefBookAttribute nextAttribute = rb.getAttribute(attribute.getRefBookAttributeId());
                return attribute.getAlias()+"."+getStackAlias(nextAttribute);
            default:
                throw new RuntimeException("Unknown RefBookAttributeType");
        }
    }

    /**
     * Метод возвращает последний не ссылочный атрибут по цепочке
     * ссылок
     *
     * @param attribute ссылочный атрибут для которого нужно получить последний не ссылочный атрибут
     * @return
     */
    private RefBookAttribute getLastAttribute(RefBookAttribute attribute){
        if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)){
            RefBook rb = getByAttribute(attribute.getRefBookAttributeId());
            RefBookAttribute nextAttribute = rb.getAttribute(attribute.getRefBookAttributeId());

            return getLastAttribute(nextAttribute);
        } else{
            return attribute;
        }
    }

    /**
     * Находится ли справочник в стандартной структуре
     *
     * @param refBookId
     * @return
     */
    private boolean isSimpleRefBool(Long refBookId){
        Long[] foreignRefBooks = new Long[]{
                RefBookDepartment.REF_BOOK_ID,
                RefBookIncome101.REF_BOOK_ID,
                RefBookIncome102.REF_BOOK_ID,
                RefBookUser.REF_BOOK_ID,
                RefBookSimpleReadOnly.FORM_TYPE_REF_BOOK_ID,
                RefBookSimpleReadOnly.SEC_ROLE_REF_BOOK_ID,
                RefBookOktmoProvider.OKTMO_REF_BOOK_ID,
                RefBookFormDataKind.REF_BOOK_ID};

        for (Long rbId : foreignRefBooks) {
            if (rbId.equals(refBookId)){
                return false;
            }
        }

        return true;
    }
}
