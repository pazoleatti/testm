package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Модель физлица для работы с журналом ФЛ по созданию 2_НДФЛ (ФЛ)
 */
@Getter
@Setter
@NoArgsConstructor
public class PersonFor2NdflFL {
    // Идентификатор
    private long id;
    // Фамилия
    private String lastName;
    // Имя
    private String firstName;
    // Отчество
    private String middleName;
    // Дата рождения
    private Date birthDate;
    // Документ включаемый в отчетность
    private IdDoc reportDoc;
    // Гражданство
    private RefBookCountry citizenship;
    // Статус налогоплательщика
    private RefBookTaxpayerState taxPayerState;
    // ИНН
    private String inn;
    // ИНН в иностранном государстве
    private String innForeign;
    // СНИЛС
    private String snils;
    // Адрес
    private Address address;
}
