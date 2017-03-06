package com.aplana.sbrf.taxaccounting.model.identification;

import java.util.Date;

/**
 * Интерфейс описывает методы получения праметров использующихся при рассчете веса и сравнении записи в НФ и записи в справочниках
 *
 * @author Andrey Drunk
 */
public interface IdentityPerson {

    String getLastName();

    String getFirstName();

    String getMiddleName();

    Integer getSex();

    String getInn();

    String getInnForeign();

    String getSnils();

    TaxpayerStatus getTaxPayerStatus();

    Country getCitizenship();

    Date getBirthDate();

    Address getAddress();

    Integer getPension();

    Integer getMedical();

    Integer getSocial();

    Integer getEmployee();

    //поле для хранения веса записи
    Double getWeigth();

    void setWeigth(Double weigth);
}
