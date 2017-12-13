package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants;
import com.aplana.sbrf.taxaccounting.model.filter.*;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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

    private final NdflPersonService ndflPersonService;

    private final RefBookFactory refBookFactory;


    public NdflPersonController(NdflPersonService ndflPersonService, RefBookFactory refBookFactory) {
        this.ndflPersonService = ndflPersonService;
        this.refBookFactory = refBookFactory;

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

        PagingResult<NdflPerson> ndflPersons = ndflPersonService.findPersonByFilter(ndflPersonFilter, pagingParams);
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
    public JqgridPagedList<NdflPersonIncomeDTO> fetchPersonIncomeData(@RequestParam NdflPersonIncomeFilter ndflPersonIncomeFilter,
                                                                   @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonIncomeDTO> ndflPersonsIncome = ndflPersonService.findPersonIncomeByFilter(ndflPersonIncomeFilter.getDeclarationDataId(), ndflPersonIncomeFilter, pagingParams);
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
    public JqgridPagedList<NdflPersonDeductionDTO> fetchPersonDeductionsData(@RequestParam NdflPersonDeductionFilter ndflPersonDeductionFilter,
                                                                          @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonDeductionDTO> ndflPersonsDeduction = ndflPersonService.findPersonDeductionsByFilter(ndflPersonDeductionFilter.getDeclarationDataId(), ndflPersonDeductionFilter, pagingParams);
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
    public JqgridPagedList<NdflPersonPrepaymentDTO> fetchPersonPrepaymentData(@RequestParam NdflPersonPrepaymentFilter ndflPersonPrepaymentFilter,
                                                                              @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonPrepaymentDTO> ndflPersonsPrepayment = ndflPersonService.findPersonPrepaymentByFilter(ndflPersonPrepaymentFilter.getDeclarationDataId(), ndflPersonPrepaymentFilter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsPrepayment,
                ndflPersonService.findPersonPrepaymentCount(ndflPersonPrepaymentFilter.getDeclarationDataId()),
                pagingParams
        );
    }


    /**
     * Возвращает список лиц подходящих условиям поиска для формирования рну ндфл
     *
     * @param ndflPersonFilter параметры фильтра
     * @return список NdflPerson заполненый данными из таблицы NDFL_PERSON
     */

    @GetMapping(value = "/rest/getListPerson/rnuPerson", params = "projection=rnuPersons")
    public JqgridPagedList<NdflPerson> getPersonList(@RequestParam NdflPersonFilter ndflPersonFilter, @RequestParam PagingParams pagingParams) {

        if (pagingParams == null){
            pagingParams = new PagingParams();
        }

        Map<String, Object> filterParams = new HashMap<String, Object>();

        if (ndflPersonFilter.getInp() != null) {
            filterParams.put(SubreportAliasConstants.INP, ndflPersonFilter.getInp());
        }
        if (ndflPersonFilter.getInnNp() != null) {
            filterParams.put("innNp", ndflPersonFilter.getInnNp());
        }

        if (ndflPersonFilter.getSnils() != null) {
            filterParams.put(SubreportAliasConstants.SNILS, ndflPersonFilter.getSnils());
        }
        if (ndflPersonFilter.getIdDocNumber() != null) {
            filterParams.put(SubreportAliasConstants.ID_DOC_NUMBER, ndflPersonFilter.getIdDocNumber());
        }
        if (ndflPersonFilter.getLastName() != null) {
            filterParams.put(SubreportAliasConstants.LAST_NAME, ndflPersonFilter.getLastName());
        }
        if (ndflPersonFilter.getFirstName() != null) {
            filterParams.put(SubreportAliasConstants.FIRST_NAME, ndflPersonFilter.getFirstName());
        }
        if (ndflPersonFilter.getMiddleName() != null) {
            filterParams.put(SubreportAliasConstants.MIDDLE_NAME, ndflPersonFilter.getMiddleName());
        }
        if (ndflPersonFilter.getDateFrom() != null) {
            filterParams.put(SubreportAliasConstants.FROM_BIRTHDAY, ndflPersonFilter.getDateFrom());
        }

        if (ndflPersonFilter.getDateTo() != null) {
            filterParams.put(SubreportAliasConstants.TO_BIRTHDAY, ndflPersonFilter.getDateTo());
        }

        PagingResult<NdflPerson> ndflPersons = ndflPersonService.findPersonByFilter(ndflPersonFilter.getDeclarationDataId(), filterParams, PagingParams.getInstance(pagingParams.getPage(), pagingParams.getCount() + 1));
        JqgridPagedList<NdflPerson> resultPerson = JqgridPagedResourceAssembler.buildPagedList(
                ndflPersons,
                ndflPersons.getTotalCount(),
                PagingParams.getInstance(pagingParams.getPage(), pagingParams.getCount())
        );
        for (NdflPerson ndflPerson : resultPerson.getRows()) {
            if (ndflPerson.getStatus() != null) {
                if (refBookFactory.getDataProvider(RefBook.Id.TAXPAYER_STATUS.getId()).
                        getRecords(null, null, "CODE = '" + ndflPerson.getStatus() + "'", null).get(0) != null) {
                    ndflPerson.setStatus(refBookFactory.getDataProvider(RefBook.Id.TAXPAYER_STATUS.getId()).
                            getRecords(null, null, "CODE = '" + ndflPerson.getStatus() + "'", null).get(0).
                            get("NAME").getStringValue());
                } else {
                    ndflPerson.setStatus("");
                }
            } else {
                ndflPerson.setStatus("");
            }
        }
        return resultPerson;
    }
}
