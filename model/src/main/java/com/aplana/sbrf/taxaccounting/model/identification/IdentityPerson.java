package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;

import java.util.Date;

/**
 * Интерфейс описывает методы получения праметров использующихся при рассчете веса и сравнении записи в НФ и записи в справочниках
 *
 * @author Andrey Drunk
 */
public interface IdentityPerson {

    Long getRecordId();

    String getLastName();

    String getFirstName();

    String getMiddleName();

    String getInn();

    String getInnForeign();

    String getSnils();

    RefBookTaxpayerState getTaxPayerState();

    RefBookCountry getCitizenship();

    Date getBirthDate();

    Address getAddress();

    //поле для хранения веса записи
    Double getWeight();

    void setWeight(Double weight);
}
