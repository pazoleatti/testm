package com.aplana.sbrf.taxaccounting.model.filter.refbook;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class RefBookPersonFilter implements Serializable {

    private Long recordId;
    private String lastName;
    private String firstName;
    private String middleName;
    private Date birthDateFrom;
    private Date birthDateTo;
    private List<Integer> terBankIds;
    private List<Department> terBanks;
    private List<Long> docTypeIds;
    private List<RefBookDocType> docTypes;
    private String documentNumber;
    private List<Long> citizenshipCountryIds;
    private List<RefBookCountry> citizenshipCountries;
    private List<Long> taxpayerStateIds;
    private List<RefBookTaxpayerState> taxpayerStates;
    private List<Long> sourceSystemIds;
    private List<RefBookAsnu> sourceSystems;
    private String id;
    private String inp;
    private String inn;
    private String innForeign;
    private String snils;
    private Boolean vip;

    // Адрес
    private String postalCode;
    private String region;
    private String district;
    private String city;
    private String locality;
    private String street;
    // Адрес за пределами РФ
    private List<Long> countryIds;
    private List<RefBookCountry> countries;
    private String foreignAddress;

    private Boolean allVersions;
    private Date versionDate;

    private Boolean duplicates;
}
