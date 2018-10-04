package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.action.PersonOriginalAndDuplicatesAction;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Контроллер для работы с записями реестра физических лиц
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
        binder.registerCustomEditor(RefBookPersonFilter.class, new RequestParamEditor(RefBookPersonFilter.class));
    }

    /**
     * Получение списка записей справочника
     *
     * @param pagingParams параметры постраничной выдачи
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/rest/refBookFL", params = "projection=common")
    public JqgridPagedList<RefBookPerson> fetchRefBookRecords(@RequestParam(required = false) RefBookPersonFilter filter,
                                                              @RequestParam(required = false) PagingParams pagingParams) {
        TAUser currentUser = securityService.currentUserInfo().getUser();
        PagingResult<RefBookPerson> records = personService.getPersons(pagingParams, filter, currentUser);
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }

    /**
     * Получение записей реестра ФЛ для назначения Оригиналом/Дубликатом
     * @param filter        фильтр выборки
     * @param pagingParams  параметры постраничной выдачи
     * @return  Страница списка записей
     */
    @GetMapping(value = "/rest/refBookFL", params = "projection=originalAndDuplicates")
    public JqgridPagedList<RefBookPerson> fetchOriginalDuplicatesCandidates(@RequestParam(required = false) RefBookPersonFilter filter,
                                                              @RequestParam(required = false) PagingParams pagingParams) {
        TAUser currentUser = securityService.currentUserInfo().getUser();
        PagingResult<RefBookPerson> records = personService.fetchOriginalDuplicatesCandidates(pagingParams, filter, currentUser);
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }


    /**
     * Получение списка ДУЛ для всех версий физлица, в т.ч. и дубликатов
     *
     * @param personId     идентификатор ФЛ
     * @param pagingParams параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/actions/refBookFL/fetchIdDocs/{personId}")
    public JqgridPagedList<Map<String, RefBookValue>> fetchIdDocs(@PathVariable Long personId, @RequestParam PagingParams pagingParams) {
        PagingResult<Map<String, RefBookValue>> resultData = personService.fetchReferencesList(personId, RefBook.Id.ID_DOC.getId(), pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(resultData, resultData.getTotalCount(), pagingParams);
    }

    /**
     * Получение списка ИНП для всех версий физлица, в т.ч. и дубликатов
     *
     * @param personId     идентификатор ФЛ
     * @param pagingParams параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/actions/refBookFL/fetchInp/{personId}")
    public JqgridPagedList<Map<String, RefBookValue>> fetchInp(@PathVariable Long personId, @RequestParam PagingParams pagingParams) {
        PagingResult<Map<String, RefBookValue>> resultData = personService.fetchReferencesList(personId, RefBook.Id.ID_TAX_PAYER.getId(), pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(resultData, resultData.getTotalCount(), pagingParams);
    }

    /**
     * Получение списка Тербанков для всех версий физлица, в т.ч. и дубликатов
     *
     * @param personId     идентификатор ФЛ
     * @param pagingParams параметры пейджинга
     * @return Страница списка значений справочника
     */
    @GetMapping(value = "/actions/refBookFL/fetchTb/{personId}")
    public JqgridPagedList<Map<String, RefBookValue>> fetchTb(@PathVariable Long personId, @RequestParam PagingParams pagingParams) {
        PagingResult<Map<String, RefBookValue>> resultData = personService.fetchReferencesList(personId, RefBook.Id.PERSON_TB.getId(), pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(resultData, resultData.getTotalCount(), pagingParams);
    }

    /**
     * Получение оригинала ФЛ
     *
     * @return оригинал ФЛ
     */
    @GetMapping(value = "/actions/refBookFL/fetchOriginal/{id}")
    public RegistryPerson fetchOriginal(@PathVariable Long id) {
        return personService.fetchOriginal(id, new Date());
    }

    /**
     * Получение оригинала ФЛ
     *
     * @param id идентификатор версии
     * @return объект версии ФЛ
     */
    @GetMapping(value = "/rest/personRegistry/fetch/{id}")
    public RegistryPerson fetchPerson(@PathVariable Long id) {
        return personService.fetchPerson(id);
    }

    /**
     * Получение списка дубликатов ФЛ
     *
     * @param personId     Идентификатор ФЛ (RECORD_ID)
     * @param pagingParams Параметры пейджинга
     * @return Страница списка дубликатов ФЛ
     */
    @GetMapping(value = "/actions/refBookFL/fetchDuplicates/{personId}")
    public JqgridPagedList<RegistryPerson> fetchDuplicates(@PathVariable Long personId, @RequestParam PagingParams pagingParams) {
        PagingResult<RegistryPerson> duplicates = personService.fetchDuplicates(personId, new Date(), pagingParams);
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
     * @return результат действия
     */
    @PostMapping(value = "/actions/refBookFL/saveOriginalAndDuplicates")
    public ActionResult saveOriginalAndDuplicates(@RequestBody PersonOriginalAndDuplicatesAction data) {
        return personService.saveOriginalAndDuplicates(securityService.currentUserInfo(), data);
    }

    @PostMapping(value = "/actions/refBookFL/export/excel")
    public ActionResult exportPersonsToExcel(@RequestParam RefBookPersonFilter filter, @RequestParam PagingParams pagingParams) {
        return personService.createTaskToCreateExcel(filter, pagingParams, securityService.currentUserInfo());
    }

    /**
     * Обновить данные записи из реестра ФЛ
     *
     * @param person данные записи из реестра ФЛ
     * @return ответ сервера
     */
    @PostMapping(value = "/actions/registryPerson/updatePerson")
    public ResponseEntity updateRegistryPerson(@RequestBody RegistryPerson person) {
        personService.updateRegistryPerson(person);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Проверть пересечение сохраняемого версий ФЛ
     * @param person объект ФЛ
     * @return стандартный ответ сервера
     */
    @PostMapping(value = "/actions/registryPerson/checkVersionOverlapping")
    public ResponseEntity checkVersionOverlapping(@RequestBody RegistryPerson person) {
        personService.checkVersionOverlapping(person);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Проверить корректность введенного ДУЛ
     * @param docCode   код документа
     * @param docNumber серия и номер документа
     * @return результат проверки
     */
    @PostMapping(value = "/actions/checkDul")
    public CheckDulResult checkDul(@RequestParam String docCode, @RequestParam String docNumber) {
        return personService.checkDul(docCode, docNumber);
    }
}
