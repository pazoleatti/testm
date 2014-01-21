package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
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
@Service("declarationTemplateOperatingService")
@Transactional
public class VersionDTOperatingServiceImpl implements VersionOperatingService<DeclarationTemplate> {

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DeclarationDataService declarationDataService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void isUsedVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        List<Long> ddIds = declarationDataService.getDeclarationDataLisByVersionTemplate(template.getId());
        if (!ddIds.isEmpty()){
            logger.error("Обнаружено использование макета для налоговых форм");
        }
    }

    @Override
    public void isCorrectVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void isIntersectionVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        List<Integer> formTemplateIds =
                declarationTemplateService.findFTVersionIntersections(template, versionActualDateEnd);
        //1 Шаг. Система проверяет пересечение с периодом актуальности хотя бы одной версии этого же макета, STATUS которой не равен -1.
        if (!formTemplateIds.isEmpty()){
            //Обнаружена хотя бы одна версия, с которой есть пересечение.
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(formTemplateIds.get(0));
            switch (declarationTemplate.getStatus()){
                case NORMAL:
                case DRAFT:
                    //Статус равен 0 или 1 и дата окончания актуальности существует.
                    DeclarationTemplate declarationTemplateEnd = declarationTemplateService.getNearestDTRight(declarationTemplate);
                    if (declarationTemplateEnd != null && declarationTemplateEnd.getVersion() != null){
                        logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                        return;
                    }
                    //Статус равен 0 или 1 и дата окончания актуальности не существует.
                    else {
                            /*calendar.setTime(formTemplate.getVersion());*/
                        //Т.к. по постановке, в случае если отсутствует дата окончания актуальности версии
                        // то датой запроса является крайний день текущего года. Крайний день всегда 31 декабря.
                            /*calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);*/
                        isUsedVersion(declarationTemplate, null, logger);
                        if (logger.containsLevel(LogLevel.ERROR)){
                            logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                            return;
                        }

                        Date ftEndDate = createActualizationDates(Calendar.DAY_OF_YEAR, -1, template.getVersion());
                        DeclarationTemplate dt = createFakeTemplate(ftEndDate, declarationTemplate);
                        declarationTemplateService.save(dt);
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
                        /*
                        Вариант когда:
                            Дата начала ее актуальности больше или равна дате начала актуальности проверяемой версии
                            и дата окончания ее актуальности больше даты окончания актуальности проверяемой версии
                            не стал реализовывать, т.к.перекрывается реализованным.
                         */
                    // Дата начала ее актуальности меньше даты начала актуальности проверяемой версии
                    if (declarationTemplate.getVersion().compareTo(template.getVersion()) >= 0){
                        DeclarationTemplate nextDeclarationTemplate = declarationTemplateService.getNearestDTRight(declarationTemplate);
                        //Система проверяет, указан ли дата окончания проверяемой версии. Дата окончания указана.
                        //Система задает дату начала актуальности обнаруженной версии, равной дате окончания проверяемой версии + 1 день.
                        if (versionActualDateEnd != null && nextDeclarationTemplate != null &&
                                nextDeclarationTemplate.getVersion().compareTo(versionActualDateEnd) >= 0){
                            declarationTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd));
                            declarationTemplateService.save(declarationTemplate);
                        }
                        //Система проверяет, указан ли дата окончания проверяемой версии. Дата окончания не указана.
                        else {
                            //Система удаляет обнаруженную версию.
                            declarationTemplateService.delete(declarationTemplate);
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
            //Следующая версия существует.
            DeclarationTemplate nextDeclarationTemplate = declarationTemplateService.getNearestDTRight(template);
            if (nextDeclarationTemplate != null){
                calendar.setTime(versionActualDateEnd);
                Calendar calendarFirstFT = Calendar.getInstance();
                calendarFirstFT.setTime(nextDeclarationTemplate.getVersion());
                Calendar distinct = Calendar.getInstance();
                distinct.setTime(new Date(calendarFirstFT.getTime().getTime() - calendar.getTime().getTime()));
                calendar.clear();
                //Разница между датами не равна 1.
                if (distinct.get(Calendar.DAY_OF_YEAR) != 1){
                    Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                    DeclarationTemplate declarationTemplate =  createFakeTemplate(date, template);
                    declarationTemplateService.save(declarationTemplate);
                }
            }
            //Следующая версия не существует.
            else{
                Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                DeclarationTemplate declarationTemplate =  createFakeTemplate(date, template);
                declarationTemplateService.save(declarationTemplate);
            }
        }
        // Дата окончания актуальности не указана.
        else {
            DeclarationTemplate nextDeclarationTemplate = declarationTemplateService.getNearestDTRight(template);
            if(nextDeclarationTemplate != null){
                template.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, -1, nextDeclarationTemplate.getVersion()));
                declarationTemplateService.save(template);
                logger.info(
                        "Установлена дата окончания актуальности версии %td.%tm.%tY в связи с наличием следующей версии",
                        template.getVersion(), template.getVersion(), template.getVersion()
                );

            }
        }
    }

    @Override
    public void createNewVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        isIntersectionVersion(template, versionActualDateEnd, logger);
    }

    @Override
    public void cleanVersions(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        DeclarationTemplate declarationTemplateFake = declarationTemplateService.getNearestDTRight(template, VersionedObjectStatus.FAKE);
        if (declarationTemplateFake != null)
            declarationTemplateService.delete(declarationTemplateFake);
    }

    private DeclarationTemplate createFakeTemplate(Date date, DeclarationTemplate realTemplate){
        DeclarationTemplate declarationTemplate =  new DeclarationTemplate();
        declarationTemplate.setVersion(date);
        declarationTemplate.setStatus(VersionedObjectStatus.FAKE);
        declarationTemplate.setEdition(0);
        declarationTemplate.setType(realTemplate.getType());
        return declarationTemplate;
    }

    private Date createActualizationDates(int calendarTime, int time, Date actualTemplateDate){
        calendar.clear();
        calendar.setTime(actualTemplateDate);
        calendar.add(calendarTime, time);
        return calendar.getTime();
    }
}
