package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.AbstractTreeListenerComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.ForeignKeyResolverComponent;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тестирование помошника лиснера
 * который отвечает за работу с внешними справочниками
 */
public class ForignKeyResolverTest {

    AbstractTreeListenerComponent foreignKeyResolverComponent;

    @Before
    public void init(){
        // Первый справочник
        RefBook refBook =  new RefBook();

        // Атрибуты
        List<RefBookAttribute> attributeList1 =  new ArrayList();
        RefBookAttribute attributeUser = new RefBookAttribute();
        attributeUser.setAttributeType(RefBookAttributeType.STRING);
        attributeUser.setRefBookId(2L);
        attributeUser.setAlias("user");
        attributeList1.add(attributeUser);
        refBook.setAttributes(attributeList1);

        // Второй справочник
        RefBook refBook2 =  new RefBook();
        // атрибуты справочника
        List<RefBookAttribute> attributeList2 =  new ArrayList();
        RefBookAttribute attributeName = new RefBookAttribute();
        attributeName.setAttributeType(RefBookAttributeType.STRING);
        attributeName.setAlias("name");
        attributeUser.setRefBookId(2L);
        attributeName.setId(2L);
        attributeList2.add(attributeName);

        RefBookAttribute attributeCity = new RefBookAttribute();
        attributeCity.setAlias("city");
        attributeCity.setRefBookId(3L);
        attributeCity.setId(3L);
        attributeList2.add(attributeCity);

        refBook2.setAttributes(attributeList2);

        // Третий справочник
        RefBook refBook3 =  new RefBook();
        List<RefBookAttribute> attributeList4 =  new ArrayList();
        RefBookAttribute attributeCityName = new RefBookAttribute();
        attributeCityName.setAlias("name");
        attributeCityName.setAttributeType(RefBookAttributeType.STRING);
        attributeCityName.setId(4L);
        attributeList4.add(attributeCityName);
        refBook3.setAttributes(attributeList4);

        RefBookDao refBookDao = mock(RefBookDao.class);
        when(refBookDao.get(1L)).thenReturn(refBook);
        when(refBookDao.get(2L)).thenReturn(refBook2);
        when(refBookDao.get(3L)).thenReturn(refBook3);


        foreignKeyResolverComponent = new ForeignKeyResolverComponent();
        foreignKeyResolverComponent.setRefBook(refBook);
        ReflectionTestUtils.setField(foreignKeyResolverComponent, "refBookDao", refBookDao);
    }

    @Test
    public void test(){
        PreparedStatementData result = new PreparedStatementData();
        foreignKeyResolverComponent.setPreparedStatementData(result);
        Filter.getFilterQuery("user.name = '123'", foreignKeyResolverComponent);

        assertTrue(result.getQuery().toString().equals("frb0.STRING_value"));
        assertTrue(result.getJoinPartsOfQuery().equals("left join ref_book_value frb0 on frb0.record_id = auser.reference_value and frb0.attribute_id = 2\n"));
    }

    @Test
    public void test2(){
        PreparedStatementData result = new PreparedStatementData();
        foreignKeyResolverComponent.setPreparedStatementData(result);
        Filter.getFilterQuery("user.city.name = '123'", foreignKeyResolverComponent);

        assertTrue(result.getJoinPartsOfQuery().equals("left join ref_book_value frb0 on frb0.record_id = auser.reference_value and frb0.attribute_id = 3\nleft join ref_book_value frb1 on frb1.record_id = frb1.city and frb1.attribute_id = 4\n"));
        assertTrue(result.getQuery().toString().equals("frb1.STRING_value"));
    }

    /**
     * Тестирование двух связей
     */
    @Test
    public void test3(){
        PreparedStatementData result1 = new PreparedStatementData();
        foreignKeyResolverComponent.setPreparedStatementData(result1);
        Filter.getFilterQuery("user.name = '123' and user.city.name = '123'", foreignKeyResolverComponent);

        assertTrue(result1.getJoinPartsOfQuery().equals("left join ref_book_value frb0 on frb0.record_id = auser.reference_value and frb0.attribute_id = 2\n" +
                "\n" +
                "left join ref_book_value frb1 on frb1.record_id = auser.reference_value and frb1.attribute_id = 3\n" +
                "left join ref_book_value frb2 on frb2.record_id = frb2.city and frb2.attribute_id = 4\n"));

    }

    /**
     * Тестирование разименовывания в случае атрибута универсального
     * справочника который ссылается на простой справочник
     */
    @Test
    public void attributeLinkFromUniversalToSimple(){

    }

    /**
     * Тестирование правильного разименовывания в случае атрибута
     * ссылающегося на разные виды справочников:
     * универальный -> универсальный -> простой
     */
    @Test
    public void attributeLinkFromUniversal2Universal2Simple(){

    }

    /**
     * Тестирование разименовывания в случае атрибута простого
     * справочника который ссылается на простой справочник
     */
    @Test
    public void attributeLinkFromSimpleToSimple(){

    }

    /**
     * Тестирование разименовывания в случае атрибута простого
     * справочника который ссылается на универсальный справочник
     */
    @Test
    public void attributeLinkFromSimpleToUniversal(){

    }

    /**
     * Тестирование правильного разименовывания в случае атрибута
     * ссылающегося на разные виды справочников:
     * простой -> универсальный -> универсальный
     */
    @Test
    public void attributeLinkFromSimple2Universal2Universal(){

    }

    /**
     * Тестирование правильного разименовывания в случае атрибута
     * ссылающегося на разные виды справочников:
     * простой -> универсальный -> простой -> универсальный
     */
    @Test
    public void attributeLinkFromSimple2Universal2Simple2Universal(){

    }
}