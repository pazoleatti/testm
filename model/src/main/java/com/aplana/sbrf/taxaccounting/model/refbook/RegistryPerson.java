package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Данные ФЛ из реестра ФЛ
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistryPerson extends IdentityObject<Long> implements PermissivePerson {

    /**
     * Общий ид всех версий ФЛ. Не изменяется.
     * В постановках "Исходный идентификатор ФЛ"
     */
    private Long oldId;
    /**
     * Общий ид всех версий ФЛ-оригинала и всех его дубликатов, причем у оригинала recordId=oldId.
     * Т.е. получается для дубликатов это ссылка на old оригинала. Изменяется.
     * В постановках "Идентификатор ФЛ"
     */
    private Long recordId;
    /**
     * Дата начала действия версии
     */
    private Date startDate;
    /**
     * Дата окончания версии
     */
    private Date endDate;
    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Отчетство
     */
    private String middleName;

    /**
     * ИНН
     */
    private String inn;

    /**
     * ИНН ИН
     */
    private String innForeign;

    /**
     * Снилс
     */
    private String snils;

    /**
     * Номер ФЛ
     */
    private Integer num;

    /**
     * Статус физлица
     */
    private RefBookTaxpayerState taxPayerState;

    /**
     * Гражданство
     */
    private RefBookCountry citizenship;

    /**
     * Дата рождения
     */
    private Date birthDate;

    private String birthPlace;

    /**
     * Список идентификаторов ФЛ
     */
    protected List<PersonIdentifier> personIdentityList;

    /**
     * ДУЛ включаемый в отчетность
     */
    private IdDoc reportDoc;

    /**
     * Список документов ФЛ
     */
    private List<IdDoc> documents;

    /**
     * Список назначенных подразделений на ФЛ
     */
    private List<PersonTb> personTbList;

    /**
     * Адрес фл
     */
    private Address address;

    /**
     * Источник (АСНУ)
     */
    private RefBookAsnu source;
    /**
     * Права
     */
    private long permissions;

    private boolean vip;

    public static final String TABLE_NAME = "ref_book_person";

    public static final String[] COLUMNS = {"id", "record_id", "old_id", "start_date", "end_date", "last_name",
            "first_name", "middle_name", "inn", "inn_foreign", "snils", "taxpayer_state", "birth_date", "citizenship",
            "report_doc", "source_id", "vip", "region_code", "postal_code", "district", "city", "locality", "street", "house",
            "build", "appartment", "country_id", "address_foreign"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "recordId", "oldId", "startDate", "endDate", "lastName", "firstName",
            "middleName", "inn", "innForeign", "snils", "taxPayerState.id", "birthDate", "citizenship.id",
            "reportDoc.id", "source.id", "vip", "address.regionCode", "address.postalCode", "address.district", "address.city",
            "address.locality", "address.street", "address.house", "address.build", "address.appartment",
            "address.country.id", "address.addressIno"};

    public RegistryPerson() {
        this.address = new Address();
        this.personIdentityList = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.personTbList = new ArrayList<>();
    }

    public String getFullName() {
        return Joiner.on(" ").skipNulls().join(Arrays.asList(lastName, firstName, middleName));
    }
}
