package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.VersionOperatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
@Service(value = "formTemplateOperatingService")
@Transactional
public class VersionOperatingServiceImpl implements VersionOperatingService<FormTemplate> {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private FormDataService formDataService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void isUsedVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        List<Long> fdIds = formDataService.getFormDataLisByVersionTemplate(template.getId());
        if (!fdIds.isEmpty()){
            logger.error("Обнаружено использование макета для налоговых форм");
        }

    }

    @Override
    public void isCorrectVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void isIntersectionVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {

        List<Integer> formTemplateIds =
                formTemplateService.findFTVersionIntersections(template, versionActualDateEnd);
        //1 Шаг. Система проверяет пересечение с периодом актуальности хотя бы одной версии этого же макета, STATUS которой не равен -1.
        if (!formTemplateIds.isEmpty()){
            System.out.println("Intersection " + formTemplateIds.get(0));
            //Обнаружена хотя бы одна версия, с которой есть пересечение.
            FormTemplate formTemplate = formTemplateService.get(formTemplateIds.get(0));
            switch (formTemplate.getStatus()){
                case NORMAL:
                case DRAFT:
                    //Статус равен 0 или 1 и дата окончания актуальности существует.
                    if (formTemplateService.getNearestFTRight(formTemplate).getVersion() != null){
                        logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                        return;
                    }
                        //Статус равен 0 или 1 и дата окончания актуальности не существует.
                    else {
                            /*calendar.setTime(formTemplate.getVersion());*/
                        //Т.к. по постановке, в случае если отсутствует дата окончания актуальности версии
                        // то датой запроса является крайний день текущего года. Крайний день всегда 31 декабря.
                            /*calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);*/
                        isUsedVersion(formTemplate, null, logger);
                        if (logger.containsLevel(LogLevel.ERROR)){
                            System.out.println();
                            logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                            return;
                        }

                        Date ftEndDate = createActualizationDates(Calendar.DAY_OF_YEAR, -1, template.getVersion());
                        FormTemplate ft = createFakeTemplate(ftEndDate, formTemplate);
                        formTemplateService.save(ft);
                        logger.info("Установлена дата окончания актуальности версии %td.%tm.%tY для предыдущей версии",
                                ftEndDate, ftEndDate, ftEndDate);
                    }
                    break;
                case FAKE:
                    //Система проверяет период актуальности обнаруженной версии.
                        /* Дата начала ее актуальности меньше даты начала актуальности проверяемой версии
                        Переход к шагу 2 основного сценария.*/

                    //Дата начала ее актуальности больше или равна дате начала актуальности проверяемой версии и
                    // дата окончания ее актуальности больше даты окончания актуальности проверяемой версии.
                        /* TODO http://conf.aplana.com/pages/viewpage.action?pageId=11377480 п. 1А.1Б.1А
                        Вариант когда:
                            Дата начала ее актуальности больше или равна дате начала актуальности проверяемой версии
                            и дата окончания ее актуальности больше даты окончания актуальности проверяемой версии
                            не стал реализовывать, т.к.перекрывается реализованным.
                         */
                    // Дата начала ее актуальности меньше даты начала актуальности проверяемой версии
                    if (formTemplate.getVersion().compareTo(template.getVersion()) >= 0){
                        FormTemplate nextFormTemplate = formTemplateService.getNearestFTRight(formTemplate);
                        //Система проверяет, указан ли дата окончания проверяемой версии. Дата окончания указана.
                        //Система задает дату начала актуальности обнаруженной версии, равной дате окончания проверяемой версии + 1 день.
                        if (versionActualDateEnd != null && nextFormTemplate != null &&
                                nextFormTemplate.getVersion().compareTo(versionActualDateEnd) >= 0){
                            formTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd));
                            formTemplateService.save(formTemplate);
                        }
                        //Система проверяет, указан ли дата окончания проверяемой версии. Дата окончания не указана.
                        else {
                            //Система удаляет обнаруженную версию.
                            formTemplateService.delete(formTemplate);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        //2 Шаг. Система проверяет наличие даты окончания актуальности.
        //Дата окончания актуальности указана.
        //Дата окончания актуальности указана и имеет значение, равное дате начала актуальности следующей версии, уменьшенной на 1 день.
        if (versionActualDateEnd != null){
            System.out.println("Exist " + versionActualDateEnd);
            //Следующая версия существует.
            FormTemplate nextFormTemplate = formTemplateService.getNearestFTRight(template);
            if (nextFormTemplate != null){
                calendar.setTime(versionActualDateEnd);
                Calendar calendarFirstFT = Calendar.getInstance();
                calendarFirstFT.setTime(nextFormTemplate.getVersion());
                Calendar distinct = Calendar.getInstance();
                distinct.setTime(new Date(calendarFirstFT.getTime().getTime() - calendar.getTime().getTime()));
                calendar.clear();
                //Разница между датами не равна 1.
                if (distinct.get(Calendar.DAY_OF_YEAR) != 1){
                    Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                    FormTemplate formTemplate =  createFakeTemplate(date, template);
                    formTemplateService.save(formTemplate);
                }
            }
            //Следующая версия не существует.
            else{
                System.out.println("Not exist : " + versionActualDateEnd);
                Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                FormTemplate formTemplate =  createFakeTemplate(date, template);
                System.out.println(formTemplateService.save(formTemplate));
            }
        }
        // Дата окончания актуальности не указана.
        else {
            FormTemplate nextFormTemplate = formTemplateService.getNearestFTRight(template);
            if(nextFormTemplate != null){
                template.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, -1, nextFormTemplate.getVersion()));
                formTemplateService.save(template);
                logger.info(
                        "Установлена дата окончания актуальности версии %td.%tm.%tY в связи с наличием следующей версии",
                        template.getVersion(), template.getVersion(), template.getVersion()
                );

            }
        }
    }

    @Override
    public void createNewVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        isIntersectionVersion(template, versionActualDateEnd, logger);
    }

    @Override
    public void cleanVersions(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        FormTemplate formTemplateFake = formTemplateService.getNearestFTRight(template, VersionedObjectStatus.FAKE);
        if (formTemplateFake != null)
            formTemplateService.delete(formTemplateFake);
    }

    private FormTemplate createFakeTemplate(Date date, FormTemplate realTemplate){
        FormTemplate formTemplate =  new FormTemplate();
        formTemplate.setVersion(date);
        formTemplate.setStatus(VersionedObjectStatus.FAKE);
        formTemplate.setEdition(0);
        formTemplate.setType(realTemplate.getType());
        formTemplate.setName("fake");
        formTemplate.setFullName("fake");
        formTemplate.setCode("fake");
        return formTemplate;
    }

    private Date createActualizationDates(int calendarTime, int time, Date actualTemplateDate){
        calendar.clear();
        calendar.setTime(actualTemplateDate);
        calendar.add(calendarTime, time);
        return calendar.getTime();
    }
}
