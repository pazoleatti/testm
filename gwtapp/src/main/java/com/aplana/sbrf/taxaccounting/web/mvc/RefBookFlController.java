package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPersonDTO;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;
import com.aplana.sbrf.taxaccounting.service.IdDocService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Контроллер для работы с записями реестра физических лиц
 */
@RestController
public class RefBookFlController {

    @Autowired
    private PersonService personService;
    @Autowired
    private IdDocService idDocService;
    @Autowired
    private SecurityService securityService;

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
    public JqgridPagedList<RegistryPersonDTO> fetchRefBookRecords(@RequestParam(required = false) RefBookPersonFilter filter,
                                                                  @RequestParam(required = false) PagingParams pagingParams) {
        PagingResult<RegistryPersonDTO> records = personService.getPersonsData(pagingParams, filter);
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }

    /**
     * Получение записей реестра ФЛ для назначения Оригиналом/Дубликатом
     *
     * @param filter       фильтр выборки
     * @param pagingParams параметры постраничной выдачи
     * @return Страница списка записей
     */
    @GetMapping(value = "/rest/refBookFL", params = "projection=originalAndDuplicates")
    public JqgridPagedList<RegistryPersonDTO> fetchOriginalDuplicatesCandidates(@RequestParam(required = false) RefBookPersonFilter filter,
                                                                                @RequestParam(required = false) PagingParams pagingParams) {
        TAUser currentUser = securityService.currentUserInfo().getUser();
        PagingResult<RegistryPersonDTO> records = personService.fetchOriginalDuplicatesCandidates(pagingParams, filter, currentUser);
        return JqgridPagedResourceAssembler.buildPagedList(records, records.getTotalCount(), pagingParams);
    }

    /**
     * Получение оригинала ФЛ
     *
     * @param id идентификатор версии
     * @return объект версии ФЛ
     */
    @GetMapping(value = "/rest/personRegistry/fetch/{id}")
    public RegistryPersonDTO fetchPerson(@PathVariable Long id) {
        return personService.fetchPersonData(id);
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
    public ResponseEntity updateRegistryPerson(@RequestBody RegistryPersonDTO person) {
        try {
            personService.updateRegistryPerson(person, securityService.currentUserInfo());
        } catch (ServiceException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServiceException("Редактирование карточки физического лица не может быть выполнено. Причина: " + e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Проверить корректность введенного ДУЛ
     *
     * @param docCode   код документа
     * @param docNumber серия и номер документа
     * @return результат проверки
     */
    @PostMapping(value = "/actions/checkDul")
    public CheckDulResult checkDul(@RequestParam String docCode, @RequestParam String docNumber) {
        return personService.checkDul(docCode, docNumber);
    }
}
