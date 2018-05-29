package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.PersonOriginalAndDuplicatesDTO;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Контроллер для работы с записями справочника "Физические лица"
 */
@RestController
public class RefBookFlController {
    private final PersonService personService;
    private final SecurityService securityService;

    public RefBookFlController(PersonService personService, SecurityService securityService) {
        this.personService = personService;
        this.securityService = securityService;
    }

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Получение списка записей справочника
     *
     * @param recordId      Идентификатор группы версий записи справочника
     * @param version       Дата актуальности для выборки записей справочника
     * @param pagingParams  Параметры пейджинга
     * @param firstName     Строка с фильтром по имени ФЛ
     * @param lastName      Строка с фильтром по фамилии ФЛ
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookFL")
    public JqgridPagedList<RefBookPerson> fetchRefBookRecords(@RequestParam(required = false) Long recordId,
                                                              @RequestParam(required = false) Date version,
                                                              @RequestParam(required = false) String firstName,
                                                              @RequestParam(required = false) String lastName,
                                                              @RequestParam(required = false) String searchPattern,
                                                              @RequestParam(required = false) boolean exactSearch,
                                                              @RequestParam PagingParams pagingParams) {
        PagingResult<RefBookPerson> records = personService.getPersons(recordId, version, pagingParams, firstName, lastName, searchPattern, exactSearch);
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }

    /**
     * Получение оригинала ФЛ
     * TODO: из-за особенностей реализации старого этот метод всегда возвращает null. С Гришей решили пока оставлять как было
     *
     * @param personId Идентификатор ФЛ (RECORD_ID)
     * @return оригинал ФЛ
     */
    @GetMapping(value = "/actions/refBookFL/fetchOriginal/{personId}")
    public RefBookPerson fetchOriginal(@PathVariable Long personId) {
        return personService.getOriginal(personId);
    }

    /**
     * Получение списка дубликатов ФЛ
     *
     * @param personId     Идентификатор ФЛ (RECORD_ID)
     * @param pagingParams Параметры пейджинга
     * @return Страница списка дубликатов ФЛ
     */
    @GetMapping(value = "/actions/refBookFL/fetchDuplicates/{personId}")
    public JqgridPagedList<RefBookPerson> fetchDuplicates(@PathVariable Long personId, @RequestParam PagingParams pagingParams) {
        PagingResult<RefBookPerson> duplicates = personService.getDuplicates(personId, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(duplicates, duplicates.getTotalCount(), pagingParams);
    }

    /**
     * Возвращает серию + номер ДУЛ ФЛ
     *
     * @param personId идентификатор ФЛ
     * @return серия + номер ДУЛ
     */
    @GetMapping(value = "/actions/refBookFL/getDocNumber/{personId}")
    public String getPersonDocNumber(@PathVariable long personId) {
        return personService.getPersonDocNumber(personId);
    }

    /**
     * Сохраняет изменения списке дубликатов и оригинале ФЛ
     *
     * @param data данные дубликатов и оригинала
     */
    @PostMapping(value = "/actions/refBookFL/saveOriginalAndDuplicates")
    public ActionResult saveOriginalAndDuplicates(@RequestBody PersonOriginalAndDuplicatesDTO data) {
        return personService.saveOriginalAndDuplicates(securityService.currentUserInfo(), data.getCurrentPerson(),
                data.getOriginal(), data.getNewDuplicates(), data.getDeletedDuplicates());
    }
}
