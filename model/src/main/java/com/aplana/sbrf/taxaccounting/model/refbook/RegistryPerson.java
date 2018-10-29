package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter @Setter
public class RegistryPerson extends IdentityObject<Long> {

    private Long recordId;

    private Date startDate;

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

    private Long oldId;

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

}
