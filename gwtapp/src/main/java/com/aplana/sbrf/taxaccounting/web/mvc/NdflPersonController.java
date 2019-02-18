package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SubreportAliasConstants;
import com.aplana.sbrf.taxaccounting.model.filter.*;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonDeductionDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDTO;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonPrepaymentDTO;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        binder.registerCustomEditor(NdflFilter.class, new RequestParamEditor(NdflFilter.class));
        binder.registerCustomEditor(NdflPersonFilter.class, new RequestParamEditor(NdflPersonFilter.class));
        binder.registerCustomEditor(NdflPersonIncomeFilter.class, new RequestParamEditor(NdflPersonIncomeFilter.class));
        binder.registerCustomEditor(NdflPersonDeductionFilter.class, new RequestParamEditor(NdflPersonDeductionFilter.class));
        binder.registerCustomEditor(NdflPersonPrepaymentFilter.class, new RequestParamEditor(NdflPersonPrepaymentFilter.class));
    }

    /**
     * Найти все данные НДФЛ ФЛ привязанные к декларации
     *
     * @param ndflFilter   параметры фильтра
     * @param pagingParams параметры для пагинации
     * @return список данных типа {@link NdflPerson}
     */
    @GetMapping(value = "/rest/ndflPerson", params = "projection=ndflPersons")
    public JqgridPagedList<NdflPerson> fetchPersonData(@RequestParam NdflFilter ndflFilter,
                                                       @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPerson> ndflPersons = ndflPersonService.findPersonByFilter(ndflFilter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersons,
                ndflPersons.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Найти все данные о доходах и НДФЛ привязанные к декларации
     *
     * @param ndflFilter параметры фильтра
     * @param pagingParams           параметры для пагинации
     * @return список данных типа {@link NdflPersonIncomeFilter}
     */
    @GetMapping(value = "/rest/ndflPerson", params = "projection=personsIncome")
    public JqgridPagedList<NdflPersonIncomeDTO> fetchPersonIncomeData(@RequestParam NdflFilter ndflFilter,
                                                                      @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonIncomeDTO> ndflPersonsIncome = ndflPersonService.findPersonIncomeByFilter(ndflFilter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsIncome,
                ndflPersonsIncome.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param ndflFilter   параметры фильтра
     * @param pagingParams параметры для пагинации
     * @return список данных типа {@link NdflPersonDeduction}
     */
    @GetMapping(value = "/rest/ndflPerson", params = "projection=personsDeduction")
    public JqgridPagedList<NdflPersonDeductionDTO> fetchPersonDeductionsData(@RequestParam NdflFilter ndflFilter,
                                                                             @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonDeductionDTO> ndflPersonsDeduction = ndflPersonService.findPersonDeductionsByFilter(ndflFilter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsDeduction,
                ndflPersonsDeduction.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Найти все данные о вычетах привязанные к декларации
     *
     * @param ndflFilter   параметры фильтра
     * @param pagingParams параметры для пагинации
     * @return список данных типа {@link NdflPersonDeduction}
     */
    @GetMapping(value = "/rest/ndflPerson", params = "projection=personsPrepayment")
    public JqgridPagedList<NdflPersonPrepaymentDTO> fetchPersonPrepaymentData(@RequestParam NdflFilter ndflFilter,
                                                                              @RequestParam PagingParams pagingParams) {

        PagingResult<NdflPersonPrepaymentDTO> ndflPersonsPrepayment = ndflPersonService.findPersonPrepaymentByFilter(ndflFilter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                ndflPersonsPrepayment,
                ndflPersonsPrepayment.getTotalCount(),
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
    public JqgridPagedList<NdflPerson> getPersonList(@RequestParam Long declarationDataId,
                                                     @RequestParam NdflPersonFilter ndflPersonFilter, @RequestParam PagingParams pagingParams) {

        if (declarationDataId == 0) {
            return null;
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
            filterParams.put(SubreportAliasConstants.FROM_BIRTHDAY, DateUtils.truncate(ndflPersonFilter.getDateFrom(), Calendar.DATE));
        }

        if (ndflPersonFilter.getDateTo() != null) {
            filterParams.put(SubreportAliasConstants.TO_BIRTHDAY, DateUtils.truncate(ndflPersonFilter.getDateTo(), Calendar.DATE));
        }

        PagingResult<NdflPerson> ndflPersons = ndflPersonService.findPersonByFilter(declarationDataId, filterParams, pagingParams);
        JqgridPagedList<NdflPerson> resultPerson = JqgridPagedResourceAssembler.buildPagedList(
                ndflPersons,
                ndflPersons.getTotalCount(),
                PagingParams.getInstance(pagingParams.getPage(), pagingParams.getCount())
        );
        for (NdflPerson ndflPerson : resultPerson.getRows()) {
            if (ndflPerson.getStatus() != null) {
                Map<String, RefBookValue> statusRecord = refBookFactory.getDataProvider(RefBook.Id.TAXPAYER_STATUS.getId()).
                        getRecords(null, null, "CODE = '" + ndflPerson.getStatus() + "'", null).get(0);
                if (statusRecord != null) {
                    ndflPerson.setStatus("(" + ndflPerson.getStatus() + ") " + statusRecord.get("NAME").getStringValue());
                } else {
                    ndflPerson.setStatus("");
                }
            } else {
                ndflPerson.setStatus("");
            }
        }
        return resultPerson;
    }

    /**
     * Найти данные ФЛ по идентификатору
     *
     * @param id   идентификатор ФЛ
     * @return объект типа {@link NdflPerson}
     */
    @GetMapping(value = "/rest/ndflPerson/{id}")
    public NdflPerson fetchPersonData(@PathVariable Long id) {
        return ndflPersonService.findOne(id);
    }

    /**
     * Возвращает наименование ДУЛ для ФЛ
     *
     * @param idDocType   Документ удостоверяющий личность.Код (Графа 10)
     * @return объект типа {@link NdflPerson}
     */
    @GetMapping(value = "/rest/getPersonDocTypeName/{idDocType}")
    public String getPersonDocTypeName(@PathVariable Long idDocType) {
        return ndflPersonService.getPersonDocTypeName(idDocType);
    }

    /**
     * Возвращяет страницу КПП, полученных из строк раздела 2 формы РНУ
     *
     * @param declarationDataId ид формы РНУ
     * @param kpp               значение поиска по КПП
     * @param pagingParams      параметры пагинации
     * @return возвращает страницу из списка КПП, полученных из строк раздела 2 формы РНУ
     */
    @GetMapping(value = "/rest/ndflPerson/kppSelect")
    public JqgridPagedList<KppSelect> findAllKppByDepartmentIdAndKpp(@RequestParam String kpp, @RequestParam int declarationDataId, @RequestParam PagingParams pagingParams) {
        PagingResult<KppSelect> result = ndflPersonService.findAllKppByDeclarationDataId(declarationDataId, kpp, pagingParams);

        return JqgridPagedResourceAssembler.buildPagedList(
                result,
                result.getTotalCount(),
                pagingParams
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccessDeniedException() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
