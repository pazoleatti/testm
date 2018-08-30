package com.aplana.sbrf.taxaccounting.model.identification;

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

    TaxpayerStatus getTaxPayerStatus();

    Country getCitizenship();

    Date getBirthDate();

    Address getAddress();

    //поле для хранения веса записи
    Double getWeight();

    void setWeight(Double weight);
}
