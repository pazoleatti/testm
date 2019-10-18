package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.util.NdflComparator;
import com.aplana.sbrf.taxaccounting.model.util.RnuNdflStringComparator;
import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

/**
 * Данные о физическом лице - получателе дохода
 */
@Getter
@Setter
public class NdflPerson extends NdflData<Long> {

    // №пп
    private Long rowNum;

    // Физическое лицо
    private Long personId;

    // Идентификатор ФЛ REF_BOOK_PERSON.RECORD_ID
    private Long recordId;

    // Идентификатор налоговой формы к которой относятся данные
    private Long declarationDataId;

    // Уникальный код клиента (Графа 2)
    //private String inp;

    // СНИЛС
    //private String snils;

    // Налогоплательщик.Фамилия (Графа 3)
    //private String lastName;

    // Налогоплательщик.Имя (Графа 4)
    //private String firstName;

    // Налогоплательщик.Отчество (Графа 5)
    //private String middleName;

    // Налогоплательщик.Дата рождения (Графа 6)
    private Date birthDay;

    // Гражданство (код страны) (Графа 7)
    private String citizenship;

    // ИНН.В Российской федерации (Графа 8)
    //private String innNp;

    // ИНН.В стране гражданства (Графа 9)
   // private String innForeign;

    // Документ удостоверяющий личность.Код (Графа 10)
    private String idDocType;

    // Документ удостоверяющий личность.Номер (Графа 11)
   // private String idDocNumber;

    // Cтатус (Код) (Графа 12)
    private String status;

    // Адрес регистрации в Российской Федерации.Код субъекта (Графа 13)
    private String regionCode;

    // Адрес регистрации в Российской Федерации.Индекс (Графа 14)
    private String postIndex;

    // Адрес регистрации в Российской Федерации.Район (Графа 15)
    //private String area;

    // Адрес регистрации в Российской Федерации.Город (Графа 16)
    //private String city;

    // Адрес регистрации в Российской Федерации.Населенный пункт (Графа 17)
   // private String locality;

    // Адрес регистрации в Российской Федерации.Улица (Графа 18)
   // private String street;

    // Адрес регистрации в Российской Федерации.Дом (Графа 19)
    //private String house;

    // Адрес регистрации в Российской Федерации.Корпус (Графа 20)
    //private String building;

    // Адрес регистрации в Российской Федерации.Квартира (Графа 21)
   // private String flat;

    // Код страны проживания вне РФ
    private String countryCode;

    // Адрес проживания вне РФ
    private String address;

    // Дополнительная информация
    private String additionalData;

    // Сведения о доходах физического лица
    private List<NdflPersonIncome> incomes;

    // Стандартные, социальные и имущественные налоговые вычеты
    private List<NdflPersonDeduction> deductions;

    // Cведения о доходах в виде авансовых платежей
    private List<NdflPersonPrepayment> prepayments;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    private Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    private String modifiedBy;

    // Идентификатор АСНУ, откуда получены данные
    private Long asnuId;

    public NdflPerson(List<NdflPersonIncome> incomes, List<NdflPersonDeduction> deductions, List<NdflPersonPrepayment> prepayments) {
        super();
        this.incomes = incomes;
        this.deductions = deductions;
        this.prepayments = prepayments;
    }

    public NdflPerson() {
        super();
        incomes = new ArrayList<>();
        deductions = new ArrayList<>();
        prepayments = new ArrayList<>();
    }

    public static final String TABLE_NAME = "ndfl_person";
    public static final String SEQ = "seq_ndfl_person";


    public static final String[] COLUMNS = {"id", "declaration_data_id", "person_id", "row_num", "inp", "snils",
            "last_name", "first_name", "middle_name", "birth_day", "citizenship", "inn_np", "inn_foreign",
            "id_doc_type", "id_doc_number", "status", "post_index", "region_code", "area", "city", "locality",
            "street", "house", "building", "flat", "country_code", "address", "additional_data", "modified_date", "modified_by", "asnu_id"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "declarationDataId", "personId", "rowNum", "inp", "snils", "lastName",
            "firstName", "middleName", "birthDay", "citizenship", "innNp", "innForeign", "idDocType", "idDocNumber",
            "status", "postIndex", "regionCode", "area", "city", "locality", "street", "house", "building", "flat",
            "countryCode", "address", "additionalData", "modifiedDate", "modifiedBy", "asnuId"};


    public List<NdflPersonIncome> getIncomes() {
        return incomes != null ? incomes : new ArrayList<NdflPersonIncome>();
    }

    public void setIncomes(List<NdflPersonIncome> incomes) {
        if (incomes != null) {
            this.incomes = incomes;
        }
    }

    public List<NdflPersonDeduction> getDeductions() {
        return deductions != null ? deductions : new ArrayList<NdflPersonDeduction>();
    }

    public void setDeductions(List<NdflPersonDeduction> deductions) {
        if (deductions != null) {
            this.deductions = deductions;
        }
    }

    public List<NdflPersonPrepayment> getPrepayments() {
        return prepayments != null ? prepayments : new ArrayList<NdflPersonPrepayment>();
    }

    public void setPrepayments(List<NdflPersonPrepayment> prepayments) {
        if (prepayments != null) {
            this.prepayments = prepayments;
        }
    }

    public String getFullName() {
        return Joiner.on(" ").skipNulls().join(Arrays.asList(lastName, firstName, middleName));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_FIELD_NAMES_STYLE).append("id", id)
                .append("rowNum", rowNum)
                .append("personId", personId)
                .append("declarationDataId", declarationDataId)
                .append("inp", inp)
                .append("snils", snils)
                .append("lastName", lastName)
                .append("firstName", firstName)
                .append("middleName", middleName)
                .append("birthDay", birthDay)
                .append("citizenship", citizenship)
                .append("innNp", innNp)
                .append("innForeign", innForeign)
                .append("idDocType", idDocType)
                .append("idDocNumber", idDocNumber)
                .append("status", status)
                .append("postIndex", postIndex)
                .append("regionCode", regionCode)
                .append("area", area)
                .append("city", city)
                .append("locality", locality)
                .append("street", street)
                .append("house", house)
                .append("building", building)
                .append("flat", flat)
                .append("countryCode", countryCode)
                .append("address", address)
                .append("additionalData", additionalData)
                .toString();
    }

    /**
     * Возвращает компаратор для сортировки физических лиц
     *
     * @return объект {@link NdflComparator} для сортировки {@link NdflPerson}
     */
    public static Comparator<NdflPerson> getComparator() {
        return new NdflComparator<NdflPerson>() {
            @Override
            public int compare(NdflPerson o1, NdflPerson o2) {
                int lastNameComp = compareValues(o1.lastName, o2.lastName, RnuNdflStringComparator.INSTANCE);
                if (lastNameComp != 0) {
                    return lastNameComp;
                }

                int firstNameComp = compareValues(o1.firstName, o2.firstName, RnuNdflStringComparator.INSTANCE);
                if (firstNameComp != 0) {
                    return firstNameComp;
                }

                int middleNameComp = compareValues(o1.middleName, o2.middleName, RnuNdflStringComparator.INSTANCE);
                if (middleNameComp != 0) {
                    return middleNameComp;
                }

                int innComp = compareValues(o1.innNp, o2.innNp, RnuNdflStringComparator.INSTANCE);
                if (innComp != 0) {
                    return innComp;
                }

                int innForeignComp = compareValues(o1.innForeign, o2.innForeign, RnuNdflStringComparator.INSTANCE);
                if (innForeignComp != 0) {
                    return innForeignComp;
                }

                int birthDayComp = compareValues(o1.birthDay, o2.birthDay, null);
                if (birthDayComp != 0) {
                    return birthDayComp;
                }

                return compareValues(o1.idDocNumber, o2.idDocNumber, RnuNdflStringComparator.INSTANCE);
            }
        };
    }

}
