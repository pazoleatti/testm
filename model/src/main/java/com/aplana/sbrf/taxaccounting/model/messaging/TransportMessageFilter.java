package com.aplana.sbrf.taxaccounting.model.messaging;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TransportMessageFilter {
    private String id;
    private List<Integer> stateIds;
    private Integer typeId;
    private String messageUuid;
    private String user;
    private Integer senderSubsystemId;
    private Integer receiverSubsystemId;
    private List<Integer> contentTypeIds;

    // Вид формы (SBRFNDFL-8318)
    private List<Integer> declarationTypes;

    private List<Integer> departmentIds;
    private String declarationId;
    private String fileName;
    private Date dateFrom;
    private Date dateTo;
}
