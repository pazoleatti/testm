package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * User: ibukanov
 * Date: 30.05.13
 * Модельный класс к журналу аудита АС "Учёт налогов"
 */
@Getter
@Setter
public class LogSystem implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private String ip;
	private int eventId;
    private String userLogin;
	private String roles;
	private String formDepartmentName;
    private String reportPeriodName;
	private String declarationTypeName;
	private String formTypeName;
	private Integer formKindId;
	private String note;
    private String userDepartmentName;
    private Integer formDepartmentId;
    private String logId;
    private Integer formTypeId;
    private Integer auditFormTypeId = 1;
    private String server;
    private int isError;

}
