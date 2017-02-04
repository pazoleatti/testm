package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

/**
 * @author Andrey Drunk
 */
@ScriptExposed
public interface FiasRefBookService {

    /**
     * Найти адресообразующий объект
     * @param regionCode код региона (обязательный параметр)
     * @param area район
     * @param city город
     * @param locality населенный пункт
     * @param street улица
     * @return адресообразующий объект справочника
     */
    List<AddressObject> findAddress(String regionCode, String area, String city, String locality, String street);

    /**
     * Найти регион по коду
     * @param regionCode код региона
     * @return адресообразующий объект справочника
     */
    AddressObject findRegionByCode(String regionCode);


}
