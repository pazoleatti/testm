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
        // Первый справочник - заказы на перевозки
        RefBook refBookOrder =  new RefBook();
        refBookOrder.setId(1L);
        refBookOrder.setName("Заказы на перевозки");

        // Атрибуты первого справочника - заказы на перевозки
        List<RefBookAttribute> attributeList1 =  new ArrayList<RefBookAttribute>();
        // атрибут - пользователь, который ответственный за перевоз груза
        RefBookAttribute attributeUser = new RefBookAttribute();
        attributeUser.setAttributeType(RefBookAttributeType.REFERENCE);
        // ссылается на справочник c id = 2
        attributeUser.setId(1L);
        attributeUser.setRefBookId(2L);
        attributeUser.setAlias("user");
        attributeList1.add(attributeUser);

        // атрибут - машина на которой будет осуществляться перевоз груза
        RefBookAttribute attributeCar = new RefBookAttribute();
        attributeCar.setAttributeType(RefBookAttributeType.STRING);
        // ссылается на справочник c id = 2
        attributeCar.setId(2L);
        attributeCar.setRefBookId(4L);
        attributeCar.setAlias("car");
        attributeList1.add(attributeCar);

        refBookOrder.setAttributes(attributeList1);

        // Второй справочник - информация о пользователях
        RefBook refBookUserInfo =  new RefBook();
        refBookUserInfo.setId(2L);
        refBookUserInfo.setName("Информация о сотрудниках");
        // атрибуты справочника
        List<RefBookAttribute> attributeList2 =  new ArrayList();
        RefBookAttribute attributeName = new RefBookAttribute();
        attributeName.setAttributeType(RefBookAttributeType.STRING);
        attributeName.setId(3L);
        attributeName.setAlias("name");
        attributeName.setRefBookId(2L);
        attributeName.setId(2L);
        attributeList2.add(attributeName);

        RefBookAttribute attributeCity = new RefBookAttribute();
        attributeCity.setAlias("city");
        attributeCity.setRefBookId(3L);
        attributeCity.setId(4L);
        attributeList2.add(attributeCity);

        refBookUserInfo.setAttributes(attributeList2);

        // Третий справочник - города
        RefBook refBookCity =  new RefBook();
        refBookCity.setId(3L);
        refBookCity.setName("Города");
        List<RefBookAttribute> attributeList3 =  new ArrayList();
        RefBookAttribute attributeCityName = new RefBookAttribute();
        attributeCityName.setAlias("name");
        attributeCityName.setAttributeType(RefBookAttributeType.STRING);
        attributeCityName.setId(5L);
        attributeList3.add(attributeCityName);
        refBookCity.setAttributes(attributeList3);

        // Четвертый справочник - автомобили (простой справочник)
        RefBook refBookCar =  new RefBook();
        refBookCar.setId(4L);
        refBookCar.setName("Автомобили");
        List<RefBookAttribute> attributeList4 =  new ArrayList<RefBookAttribute>();
        RefBookAttribute attributeBrand = new RefBookAttribute();
        attributeBrand.setAlias("brand");
        attributeBrand.setAttributeType(RefBookAttributeType.STRING);
        attributeBrand.setId(6L);
        attributeList4.add(attributeBrand);
        refBookCar.setAttributes(attributeList4);

        RefBookDao refBookDao = mock(RefBookDao.class);
        when(refBookDao.get(1L)).thenReturn(refBookOrder);
        when(refBookDao.get(2L)).thenReturn(refBookUserInfo);
        when(refBookDao.get(3L)).thenReturn(refBookCity);
        when(refBookDao.get(4L)).thenReturn(refBookCar);


        foreignKeyResolverComponent = new ForeignKeyResolverComponent();
        foreignKeyResolverComponent.setRefBook(refBookOrder);
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

        assertTrue(result.getJoinPartsOfQuery().equals("left join ref_book_value frb0 on frb0.record_id = auser.reference_value and frb0.attribute_id = 4\nleft join ref_book_value frb1 on frb1.record_id = frb1.city and frb1.attribute_id = 5\n"));
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
                "left join ref_book_value frb1 on frb1.record_id = auser.reference_value and frb1.attribute_id = 4\n" +
                "left join ref_book_value frb2 on frb2.record_id = frb2.city and frb2.attribute_id = 5\n"));

    }

    /**
     * Тестирование разименовывания в случае атрибута универсального
     * справочника который ссылается на простой справочник
     */
    @Test
    public void attributeLinkFromUniversalToSimple(){
        PreparedStatementData result = new PreparedStatementData();
        foreignKeyResolverComponent.setPreparedStatementData(result);
        Filter.getFilterQuery("car.brand like '%Honda%' ", foreignKeyResolverComponent);
        assertTrue(result.getJoinPartsOfQuery().equals("left join car frb0 on frb0.id = acar.reference_value\n"));
        assertTrue(result.getQuery().toString().equals("frb0.brand"));
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