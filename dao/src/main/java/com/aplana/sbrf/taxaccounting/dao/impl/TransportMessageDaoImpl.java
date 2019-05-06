package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TransportMessageDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.messaging.*;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.joda.time.LocalDateTime;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


@Repository
public class TransportMessageDaoImpl extends AbstractDao implements TransportMessageDao {

    @Language("sql")
    private static final String SELECT_TRANSPORT_MESSAGE = "\n" +
            "select \n" +
            "   tm.id, tm.message_uuid, tm.datetime, tm.type, tm.content_type, tm.state, \n" +
            "   tm.sender_subsystem_id sender_id, sender.code sender_code, sender.name sender_name, sender.short_name sender_short_name, \n" +
            "   tm.receiver_subsystem_id receiver_id, receiver.code receiver_code, receiver.name receiver_name, receiver.short_name receiver_short_name, \n" +
            "   tm.blob_id, blob_data.name blob_name, tm.source_file_name, tm.explanation, \n" +
            "   tm.initiator_user_id user_id, u.login user_login, u.name user_name,  \n" +
            "   tm.declaration_id, declaration_template.name declaration_type_name, \n" +
            "   department.id department_id, department.name department_name, \n" +
            "   extract(year from report_period.start_date) period_year, report_period.name period_name, \n" +
            "   case \n" +
            "       when tm.body is not null then 1 else 0 \n" +
            "   end as has_body \n" +
            "from transport_message tm \n" +
            "left join vw_subsystem_syn sender on tm.sender_subsystem_id = sender.id \n" +
            "left join vw_subsystem_syn receiver on tm.receiver_subsystem_id = receiver.id \n" +
            "left join sec_user u on tm.initiator_user_id = u.id \n" +
            "left join declaration_data decl on tm.declaration_id = decl.id \n" +
            "left join declaration_template on decl.declaration_template_id = declaration_template.id \n" +
            "left join department_report_period drp on decl.department_report_period_id = drp.id \n" +
            "left join department on drp.department_id = department.id \n" +
            "left join report_period on drp.report_period_id = report_period.id \n" +
            "left join blob_data on tm.blob_id = blob_data.id \n";

    @Override
    public TransportMessage findById(Long id) {
        try {
            String sql = SELECT_TRANSPORT_MESSAGE + " where tm.id = ?";
            return getJdbcTemplate().queryForObject(sql, new Object[]{id}, new int[]{Types.NUMERIC}, new TransportMessageMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public String findMessageBodyById(Long id) {
        try {
            String sql = "select body from transport_message where id = ?";
            return getJdbcTemplate().queryForObject(sql, new Object[]{id}, new int[]{Types.NUMERIC}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<TransportMessage> findByFilter(TransportMessageFilter filter, PagingParams pagingParams) {

        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filter != null) {
            if (StringUtils.isNotEmpty(filter.getId())) {
                conditions.add(" (tm.id like :id) ");
                params.addValue("id", "%" + filter.getId() + "%");
            }
            if (CollectionUtils.isNotEmpty(filter.getStateIds())) {
                conditions.add(" (tm.state in (:stateIds)) ");
                params.addValue("stateIds", filter.getStateIds());
            }
            if (filter.getTypeId() != null) {
                conditions.add(" (tm.type = :typeId) ");
                params.addValue("typeId", filter.getTypeId());
            }
            if (StringUtils.isNotEmpty(filter.getMessageUuid())) {
                conditions.add(" (lower(tm.message_uuid) like :messageUuid) ");
                params.addValue("messageUuid", "%" + filter.getMessageUuid().toLowerCase() + "%");
            }
            if (StringUtils.isNotEmpty(filter.getUser())) {
                conditions.add(" ((lower(u.login) like :user) or (lower(u.name) like :user)) ");
                params.addValue("user", "%" + filter.getUser() + "%");
            }
            if (filter.getSenderSubsystemId() != null) {
                conditions.add(" (tm.sender_subsystem_id = :senderId) ");
                params.addValue("senderId", filter.getSenderSubsystemId());
            }
            if (filter.getReceiverSubsystemId() != null) {
                conditions.add(" (tm.receiver_subsystem_id = :receiverId) ");
                params.addValue("receiverId", filter.getReceiverSubsystemId());
            }
            if (CollectionUtils.isNotEmpty(filter.getContentTypeIds())) {
                conditions.add(" (tm.content_type in (:contentTypes)) ");
                params.addValue("contentTypes", filter.getContentTypeIds());
            }
            if (StringUtils.isNotEmpty(filter.getDeclarationId())) {
                conditions.add(" (tm.declaration_id like :declarationId) ");
                params.addValue("declarationId", "%" + filter.getDeclarationId() + "%");
            }
            if (CollectionUtils.isNotEmpty(filter.getDepartmentIds())) {
                conditions.add(" (department.id in (:departmentIds)) ");
                params.addValue("departmentIds", filter.getDepartmentIds());
            }
            if (StringUtils.isNotEmpty(filter.getFileName())) {
                conditions.add(" (lower(blob_data.name) like :fileName) ");
                params.addValue("fileName", "%" + filter.getFileName().toLowerCase() + "%");
            }
            if (filter.getDateFrom() != null) {
                conditions.add(" (tm.datetime >= :dateFrom) ");
                params.addValue("dateFrom", filter.getDateFrom());
            }
            if (filter.getDateTo() != null) {
                conditions.add(" (tm.datetime <= :dateTo) ");
                params.addValue("dateTo", filter.getDateTo());
            }
        }

        String sql;
        if (conditions.size() > 0) {
            sql = SELECT_TRANSPORT_MESSAGE + " where \n" + StringUtils.join(conditions, " \n and ");
        } else {
            sql = SELECT_TRANSPORT_MESSAGE;
        }

        if (pagingParams != null) {
            sql = pagingParams.wrapQuery(sql, params);
        }

        return getNamedParameterJdbcTemplate().query(sql, params, new TransportMessageMapper());
    }


    private static final class TransportMessageMapper implements RowMapper<TransportMessage> {
        @Override
        public TransportMessage mapRow(ResultSet rs, int i) throws SQLException {

            TransportMessage message = new TransportMessage();
            message.setId(SqlUtils.getLong(rs, "id"));
            message.setMessageUuid(rs.getString("message_uuid"));
            message.setDateTime(LocalDateTime.fromDateFields(rs.getTimestamp("datetime")));
            message.setType(TransportMessageType.fromInt(rs.getInt("type")));
            message.setContentType(TransportMessageContentType.fromInt(rs.getInt("content_type")));
            message.setState(TransportMessageState.fromInt(rs.getInt("state")));
            message.setSourceFileName(rs.getString("source_file_name"));
            message.setExplanation(rs.getString("explanation"));
            message.setHasBody(rs.getBoolean("has_body"));

            // Данные о форме
            String periodYear = rs.getString("period_year");
            String periodName = rs.getString("period_name");
            DeclarationShortInfo declaration = DeclarationShortInfo.builder()
                    .id(SqlUtils.getLong(rs, "declaration_id"))
                    .departmentName(rs.getString("department_name"))
                    .typeName(rs.getString("declaration_type_name"))
                    .reportPeriodName(Joiner.on(", ").skipNulls().join(periodYear, periodName))
                    .build();
            message.setDeclaration(declaration);

            // Данные о файле
            BlobDto blob = new BlobDto();
            blob.setUuid(rs.getString("blob_id"));
            blob.setName(rs.getString("blob_name"));
            message.setBlob(blob);

            // Поле "Пользователь-инициатор"
            TAUser user = TAUser.builder()
                    .id(rs.getInt("user_id"))
                    .login(rs.getString("user_login"))
                    .name(rs.getString("user_name"))
                    .roles(new ArrayList<TARole>())
                    .build();
            message.setInitiatorUser(user);

            // Поля типа "Подсистема АС УН"
            Long senderSubsystemId = SqlUtils.getLong(rs, "sender_id");
            if (senderSubsystemId != null) {
                Subsystem sender = Subsystem.builder()
                        .id(senderSubsystemId)
                        .code(rs.getString("sender_code"))
                        .name(rs.getString("sender_name"))
                        .shortName(rs.getString("sender_short_name"))
                        .build();
                message.setSenderSubsystem(sender);
            }
            Long receiverSubsystemId = SqlUtils.getLong(rs, "receiver_id");
            if (receiverSubsystemId != null) {
                Subsystem receiver = Subsystem.builder()
                        .id(receiverSubsystemId)
                        .code(rs.getString("receiver_code"))
                        .name(rs.getString("receiver_name"))
                        .shortName(rs.getString("receiver_short_name"))
                        .build();
                message.setReceiverSubsystem(receiver);
            }

            return message;
        }
    }
}
