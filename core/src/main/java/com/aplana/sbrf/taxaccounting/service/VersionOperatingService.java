package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;

/**
 * User: avanteev
 */
public interface VersionOperatingService<T> {
    void isUsedVersion(T template, Date versionActualDateEnd, Logger logger);
    void isCorrectVersion(T template, Date versionActualDateEnd, Logger logger);
    void isIntersectionVersion(T template, Date versionActualDateEnd, Logger logger);
    void createNewVersion(T template, Date versionActualDateEnd, Logger logger);
    void cleanVersions(T template, Date versionActualDateEnd, Logger logger);
}
