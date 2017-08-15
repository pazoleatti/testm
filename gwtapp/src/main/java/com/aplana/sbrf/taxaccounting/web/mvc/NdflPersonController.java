package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.*;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для работы с ФЛ в ПНФ
 */

@RestController
public class NdflPersonController {

    private NdflPersonService ndflPersonService;

    public NdflPersonController(NdflPersonService ndflPersonService) {
        this.ndflPersonService = ndflPersonService;
    }

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(NdflPersonFilter.class, new RequestParamEditor(NdflPersonFilter.class));
        binder.registerCustomEditor(NdflPersonIncomeFilter.class, new RequestParamEditor(NdflPersonIncomeFilter.class));
        binder.registerCustomEditor(NdflPersonDeductionFilter.class, new RequestParamEditor(NdflPersonDeductionFilter.class));
        binder.registerCustomEditor(NdflPersonPrepaymentFilter.class, new RequestParamEditor(NdflPersonPrepaymentFilter.class));
    }

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param ndflPersonFilter параметры фильтра
     * @param pagingParams     параметры для пагинации
     * @return список данных типа {@link NdflPerson}
     */
    @GetMapping(value = "/rest/ndflPerson", params = "projection=ndflPersons")
    public JqgridPagedList<NdflPerson> fetchPersonData(@RequestParam NdflPersonFilter ndflPersonFilter,
                                                       @RequestParam PagingParams pagingParams) {

        Map<String, Object> filterParams = new HashMap<String, Object>();

        if (ndflPersonFilter.getInp() != null) {
            filterParams.put("inp", ndflPersonFilter.getInp());
        }
        if (ndflPersonFilter.getInnNp() != null) {
            filterParams.put("innNp", ndflPersonFilter.getInnNp());
        }
        if (ndflPersonFilter.getInnForeign() != null) {
            filterParams.put("innForeign", ndflPersonFilter.getInnForeign());
        }
        if (ndflPersonFilter.getSnils() != null) {
            filterParams.put("snils", ndflPersonFilter.getSnils());
        }
        if (ndflPersonFilter.getIdDocNumber() != null) {
            filterParams.put("idDocNumber", ndflPersonFilter.getIdDocNumber());
        }
        if (ndflPersonFilter.getLastName() != null) {
            filterParams.put("lastName", ndflPersonFilter.getLastName());
        }
        if (ndflPersonFilter.getFirstName() != null) {
            filterParams.put("firstName", ndflPersonFilter.getFirstName());
        }
        if (ndflPersonFilter.getMiddleName() != null) {
            filterParams.put("middleName", ndflPersonFilter.getMiddleName());
        }
        if (ndflPersonFilter.getDateFrom() != null) {
            filterParams.put("fromBirthDay", ndflPersonFilter.getDateFrom());
        }
        if (ndflPersonFilter.getDateTo() != null) {
            filterParams.put("toBirthDay", ndflPersonFilter.getDateTo());
        }

        PagingResult<NdflPerson> ndflPersons = ndflPersonService.findPersonByFilter(ndflPersonFilter.getDeclarationDataId(), filterParams, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersons,
                ndflPersonService.findPersonCount(ndflPersonFilter.getDeclarationDataId()),
                pagingParams
        );
    }

    /**
     * Найти все данные о доходах и НДФЛ привязанные к декларации
     *
     * @param ndflPersonIncomeFilter параметры фильтра
     * @param pagingParams           параметры для пагинации
     * @return список данных типа {@link NdflPersonIncomeFilter}
     */
    @GetMapping(value = "/rest/ndflPerson", params = "projection=personsIncome")
    public JqgridPagedList<NdflPersonIncome> fetchPersonIncomeData(@RequestParam NdflPersonIncomeFilter ndflPersonIncomeFilter,
                                                                   @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonIncome> ndflPersonsIncome = ndflPersonService.findPersonIncomeByFilter(ndflPersonIncomeFilter.getDeclarationDataId(), ndflPersonIncomeFilter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsIncome,
                ndflPersonService.findPersonIncomeCount(ndflPersonIncomeFilter.getDeclarationDataId()),
                pagingParams
        );
    }

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param ndflPersonDeductionFilter параметры фильтра
     * @param pagingParams              параметры для пагинации
     * @return список данных типа {@link NdflPersonDeduction}
     */
    @GetMapping(value = "/rest/ndflPerson", params = "projection=personsDeduction")
    public JqgridPagedList<NdflPersonDeduction> fetchPersonDeductionsData(@RequestParam NdflPersonDeductionFilter ndflPersonDeductionFilter,
                                                                          @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonDeduction> ndflPersonsDeduction = ndflPersonService.findPersonDeductionsByFilter(ndflPersonDeductionFilter.getDeclarationDataId(), ndflPersonDeductionFilter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsDeduction,
                ndflPersonService.findPersonDeductionsCount(ndflPersonDeductionFilter.getDeclarationDataId()),
                pagingParams
        );
    }

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param ndflPersonPrepaymentFilter параметры фильтра
     * @param pagingParams               параметры для пагинации
     * @return список данных типа {@link NdflPersonDeduction}
     */
    @GetMapping(value = "/rest/ndflPerson", params = "projection=personsPrepayment")
    public JqgridPagedList<NdflPersonPrepayment> fetchPersonPrepaymentData(@RequestParam NdflPersonPrepaymentFilter ndflPersonPrepaymentFilter,
                                                                           @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonPrepayment> ndflPersonsPrepayment = ndflPersonService.findPersonPrepaymentByFilter(ndflPersonPrepaymentFilter.getDeclarationDataId(), ndflPersonPrepaymentFilter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsPrepayment,
                ndflPersonService.findPersonPrepaymentCount(ndflPersonPrepaymentFilter.getDeclarationDataId()),
                pagingParams
        );
    }
}
