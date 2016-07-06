package com.aplana.sbrf.taxaccounting.templateversion;

import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Date;

/**
 * User: avanteev
 */
public interface VersionOperatingService {
    boolean isUsedVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger);
    void isCorrectVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger);
    void isIntersectionVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger);
    void cleanVersions(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger);

    /**
     * Проверка источников-приемников за определенные даты
     * @param typeId макет
     * @param versionActualDateStart дата начала проверки(дата начала актуальности версии макета)
     * @param versionActualDateEnd дата окончания (версии макета)
     */
    void checkDestinationsSources(int typeId, Date versionActualDateStart, Date versionActualDateEnd, Logger logger);

    /**
     * Проверка источников-приемников.
     * Ищутся и.-пр., которые попали промежуток изменения начала даты актульности макета.
     * Если дата окончания актульности изменена на большую или совсем отсутствует, то нет необходимости поиска и.-пр.
     * @param typeId макет
     * @param beginRange область изменения дат начала (изменение даты актульности)
     * @param endRange дат окончания (изменение даты окончания актульности)
     */
    void checkDestinationsSources(int typeId, Pair<Date,Date> beginRange, Pair<Date,Date> endRange, Logger logger);
}
