package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.*;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для работы с ФЛ в ПНФ
 */

@Controller
public class NdflPersonRestController {

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        binder.registerCustomEditor(NdflPersonFilter.class, new RequestParamEditor(NdflPersonFilter.class));
        binder.registerCustomEditor(NdflPersonIncomeFilter.class, new RequestParamEditor(NdflPersonIncomeFilter.class));
        binder.registerCustomEditor(NdflPersonDeductionFilter.class, new RequestParamEditor(NdflPersonDeductionFilter.class));
        binder.registerCustomEditor(NdflPersonPrepaymentFilter.class, new RequestParamEditor(NdflPersonPrepaymentFilter.class));
    }

    @Autowired
    private NdflPersonService ndflPersonService;

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param rows              параметр для пагинации
     * @param page              параметр для пагинации
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */
    @RequestMapping(value = "/rest/ndflPerson/get", method = RequestMethod.GET, params = "projection=getPersons")
    @ResponseBody
    public JqgridPagedList<NdflPerson> fetchPersonData(@RequestParam NdflPersonFilter ndflPersonFilter,
                                                       @RequestParam int page,
                                                       @RequestParam int rows) {

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

        int startIndex = 1;
        if (page != 1) {
            startIndex = rows * (page - 1) + 1;
        }

        int totalCount = ndflPersonService.findPersonCount(ndflPersonFilter.getDeclarationDataId());
        PagingResult<NdflPerson> ndflPersons = ndflPersonService.findPersonByFilter(ndflPersonFilter.getDeclarationDataId(), filterParams, startIndex, rows);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersons,
                totalCount,
                page, rows
        );
    }

    /**
     * Найти все данные о доходах и НДФЛ привязанные к декларации
     *
     * @param page              параметр для пагинации
     * @param rows              параметр для пагинации
     * @return список NdflPersonIncomeFilter заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_INCOME
     */
    @RequestMapping(value = "/rest/ndflPerson/get", method = RequestMethod.GET, params = "projection=getPersonsIncome")
    @ResponseBody
    public JqgridPagedList<NdflPersonIncome> fetchPersonIncomeData(@RequestParam NdflPersonIncomeFilter ndflPersonIncomeFilter,
                                                                   @RequestParam int page,
                                                                   @RequestParam int rows) {
        Map<String, Object> filterParams = new HashMap<String, Object>();
        if (ndflPersonIncomeFilter.getInp() != null) {
            filterParams.put("inp", ndflPersonIncomeFilter.getInp());
        }
        if (ndflPersonIncomeFilter.getOperationId() != null) {
            filterParams.put("operationId", ndflPersonIncomeFilter.getOperationId());
        }
        if (ndflPersonIncomeFilter.getKpp() != null) {
            filterParams.put("kpp", ndflPersonIncomeFilter.getKpp());
        }
        if (ndflPersonIncomeFilter.getOktmo() != null) {
            filterParams.put("oktmo", ndflPersonIncomeFilter.getOktmo());
        }
        if (ndflPersonIncomeFilter.getIncomeCode() != null) {
            filterParams.put("incomeCode", ndflPersonIncomeFilter.getIncomeCode());
        }
        if (ndflPersonIncomeFilter.getIncomeAttr() != null) {
            filterParams.put("incomeAttr", ndflPersonIncomeFilter.getIncomeAttr());
        }
        if (ndflPersonIncomeFilter.getTaxRate() != null) {
            filterParams.put("taxRate", ndflPersonIncomeFilter.getTaxRate());
        }
        if (ndflPersonIncomeFilter.getNumberPaymentOrder() != null) {
            filterParams.put("numberPaymentOrder", ndflPersonIncomeFilter.getNumberPaymentOrder());
        }
        if (ndflPersonIncomeFilter.getTransferDateFrom() != null) {
            filterParams.put("transferDateFrom", ndflPersonIncomeFilter.getTransferDateFrom());
        }
        if (ndflPersonIncomeFilter.getTransferDateTo() != null) {
            filterParams.put("transferDateTo", ndflPersonIncomeFilter.getTransferDateTo());
        }
        if (ndflPersonIncomeFilter.getCalculationDateFrom() != null) {
            filterParams.put("calculationDateFrom", ndflPersonIncomeFilter.getCalculationDateFrom());
        }
        if (ndflPersonIncomeFilter.getCalculationDateTo() != null) {
            filterParams.put("calculationDateTo", ndflPersonIncomeFilter.getCalculationDateTo());
        }
        if (ndflPersonIncomeFilter.getPaymentDateFrom() != null) {
            filterParams.put("paymentDateFrom", ndflPersonIncomeFilter.getPaymentDateFrom());
        }
        if (ndflPersonIncomeFilter.getPaymentDateTo() != null) {
            filterParams.put("paymentDateTo", ndflPersonIncomeFilter.getPaymentDateTo());
        }

        int startIndex = 1;
        if (page != 1) {
            startIndex = rows * (page - 1) + 1;
        }

        int totalCount = ndflPersonService.findPersonIncomeCount(ndflPersonIncomeFilter.getDeclarationDataId());
        PagingResult<NdflPersonIncome> ndflPersonsIncome = ndflPersonService.findPersonIncomeByFilter(ndflPersonIncomeFilter.getDeclarationDataId(), filterParams, startIndex, rows);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsIncome,
                totalCount,
                page, rows
        );
    }

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param page              параметр для пагинации
     * @param rows              параметр для пагинации
     * @return список NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    @RequestMapping(value = "/rest/ndflPerson/get", method = RequestMethod.GET, params = "projection=getPersonsDeduction")
    @ResponseBody
    public JqgridPagedList<NdflPersonDeduction> fetchPersonDeductionsData(@RequestParam NdflPersonDeductionFilter ndflPersonDeductionFilter,
                                                                          @RequestParam int page,
                                                                          @RequestParam int rows) {
        Map<String, Object> filterParams = new HashMap<String, Object>();
        if (ndflPersonDeductionFilter.getInp() != null) {
            filterParams.put("inp", ndflPersonDeductionFilter.getInp());
        }
        if (ndflPersonDeductionFilter.getOperationId() != null) {
            filterParams.put("operationId", ndflPersonDeductionFilter.getOperationId());
        }
        if (ndflPersonDeductionFilter.getDeductionCode() != null) {
            filterParams.put("deductionCode", ndflPersonDeductionFilter.getDeductionCode());
        }
        if (ndflPersonDeductionFilter.getIncomeCode() != null) {
            filterParams.put("incomeCode", ndflPersonDeductionFilter.getIncomeCode());
        }
        if (ndflPersonDeductionFilter.getCalculationDateFrom() != null) {
            filterParams.put("calculationDateFrom", ndflPersonDeductionFilter.getCalculationDateFrom());
        }
        if (ndflPersonDeductionFilter.getCalculationDateTo() != null) {
            filterParams.put("calculationDateTo", ndflPersonDeductionFilter.getCalculationDateTo());
        }
        if (ndflPersonDeductionFilter.getDeductionDateFrom() != null) {
            filterParams.put("deductionDateFrom", ndflPersonDeductionFilter.getDeductionDateFrom());
        }
        if (ndflPersonDeductionFilter.getDeductionDateTo() != null) {
            filterParams.put("deductionDateTo", ndflPersonDeductionFilter.getDeductionDateTo());
        }

        int startIndex = 1;
        if (page != 1) {
            startIndex = rows * (page - 1) + 1;
        }

        int totalCount = ndflPersonService.findPersonDeductionsCount(ndflPersonDeductionFilter.getDeclarationDataId());
        PagingResult<NdflPersonDeduction> ndflPersonsDeduction = ndflPersonService.findPersonDeductionsByFilter(ndflPersonDeductionFilter.getDeclarationDataId(), filterParams, startIndex, rows);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsDeduction,
                totalCount,
                page, rows
        );
    }

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param page              параметр для пагинации
     * @param rows              параметр для пагинации
     * @return список NdflPersonDeduction заполненый данными из таблиц NDFL_PERSON и NDFL_PERSON_DEDUCTION
     */
    @RequestMapping(value = "/rest/ndflPerson/get", method = RequestMethod.GET, params = "projection=getPersonsPrepayment")
    @ResponseBody
    public JqgridPagedList<NdflPersonPrepayment> fetchPersonPrepaymentData(@RequestParam NdflPersonPrepaymentFilter ndflPersonPrepaymentFilter,
                                                                           @RequestParam int page,
                                                                           @RequestParam int rows) {
        Map<String, Object> filterParams = new HashMap<String, Object>();
        if (ndflPersonPrepaymentFilter.getInp() != null) {
            filterParams.put("inp", ndflPersonPrepaymentFilter.getInp());
        }
        if (ndflPersonPrepaymentFilter.getOperationId() != null) {
            filterParams.put("operationId", ndflPersonPrepaymentFilter.getOperationId());
        }
        if (ndflPersonPrepaymentFilter.getNotifNum() != null) {
            filterParams.put("notifNum", ndflPersonPrepaymentFilter.getNotifNum());
        }
        if (ndflPersonPrepaymentFilter.getNotifSource() != null) {
            filterParams.put("notifSource", ndflPersonPrepaymentFilter.getNotifSource());
        }
        if (ndflPersonPrepaymentFilter.getNotifDateFrom() != null) {
            filterParams.put("notifDateFrom", ndflPersonPrepaymentFilter.getNotifDateFrom());
        }
        if (ndflPersonPrepaymentFilter.getNotifDateTo() != null) {
            filterParams.put("notifDateTo", ndflPersonPrepaymentFilter.getNotifDateTo());
        }

        int startIndex = 1;
        if (page != 1) {
            startIndex = rows * (page - 1) + 1;
        }

        int totalCount = ndflPersonService.findPersonPrepaymentCount(ndflPersonPrepaymentFilter.getDeclarationDataId());

        PagingResult<NdflPersonPrepayment> ndflPersonsPrepayment = ndflPersonService.findPersonPrepaymentByFilter(ndflPersonPrepaymentFilter.getDeclarationDataId(), filterParams, startIndex, rows);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsPrepayment,
                totalCount,
                page, rows
        );
    }
}
