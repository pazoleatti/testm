package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TransportMessageDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.Subsystem;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.messaging.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;


@RunWith(SpringRunner.class)
@ContextConfiguration({"TransportMessageDaoTest.xml"})
@DirtiesContext
public class TransportMessageDaoTest {

    private static final int FULL_TABLE_SIZE = 4;

    @Autowired
    private TransportMessageDao transportMessageDao;

    private TransportMessageFilter filter = new TransportMessageFilter();
    private PagingParams pagingParams = new PagingParams();

    /**
     * Возникали ошибки при сериализации в json, добавил этот тест.
     */
    @Test
    public void test_resultCanBeUsedForJson() throws JsonProcessingException {
        List<TransportMessage> result = transportMessageDao.findByFilter(null, null);
        assertThat(result).isNotEmpty();

        ObjectMapper mapper = new ObjectMapper();
        String jsonValue = mapper.writeValueAsString(result.get(0));
        assertThat(jsonValue).isNotEmpty();
    }


    @Test
    public void test_findById() {
        TransportMessage message = transportMessageDao.findById(1L);
        assertThat(message)
                .isNotNull()
                .extracting("id")
                .containsOnly(1L);
    }

    @Test
    public void test_findMessageBodyById() {
        String body = transportMessageDao.findMessageBodyById(1L);
        assertThat(body).isEqualTo("<xml></xml>");
    }


    @Test
    public void test_hasBody_onExistent() {
        TransportMessage message = transportMessageDao.findById(1L);
        assertThat(message.hasBody()).isTrue();
    }

    @Test
    public void test_hasBody_onNonExistent() {
        TransportMessage message = transportMessageDao.findById(2L);
        assertThat(message.hasBody()).isFalse();
    }


    @Test
    public void test_findByFilter_onNull() {
        List<TransportMessage> result = transportMessageDao.findByFilter(null, pagingParams);
        assertThat(result).hasSize(FULL_TABLE_SIZE);
    }

    @Test
    public void test_findByFilter_onEmpty() {
        List<TransportMessage> result = transportMessageDao.findByFilter(filter, pagingParams);
        assertThat(result).hasSize(FULL_TABLE_SIZE);
    }

    @Test
    public void test_findByFilter_byId() {
        filter.setId("1");
        List<TransportMessage> result = transportMessageDao.findByFilter(filter, pagingParams);
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .extracting("id").containsOnly(1L);
    }

    @Test
    public void test_findByFilter_byStateIds() {
        filter.setStateIds(Arrays.asList(1, 2));
        List<TransportMessage> result = transportMessageDao.findByFilter(filter, pagingParams);
        assertThat(result)
                .isNotEmpty()
                .extracting("state")
                .containsOnly(TransportMessageState.CONFIRMED, TransportMessageState.ERROR);
    }

    @Test
    public void test_findByFilter_byTypeId() {
        filter.setTypeId(0);
        List<TransportMessage> result = transportMessageDao.findByFilter(filter, pagingParams);
        assertThat(result)
                .isNotEmpty()
                .extracting("type")
                .containsOnly(TransportMessageType.OUTGOING);
    }

    @Test
    public void test_findByFilter_byMessageUuid() {
        filter.setMessageUuid("bc12");
        List<TransportMessage> result = transportMessageDao.findByFilter(filter, pagingParams);
        assertThat(result)
                .isNotEmpty()
                .hasSize(2);
        assertThat(result.get(0).getMessageUuid()).containsIgnoringCase("bc12");
        assertThat(result.get(1).getMessageUuid()).containsIgnoringCase("bc12");
    }

    @Test
    public void test_findByFilter_byDeclarationId() {
        filter.setDeclarationId("1");
        List<TransportMessage> result = transportMessageDao.findByFilter(filter, pagingParams);
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .extracting("declaration.id")
                .containsExactly(1L);
    }

    @Test
    public void test_findByFilter_byDepartmentId() {
        filter.setDepartmentIds(Arrays.asList(1, 2));
        List<TransportMessage> result = transportMessageDao.findByFilter(filter, pagingParams);
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .extracting("declaration.departmentName")
                .containsExactly("Банк");
    }

    @Test
    public void test_findByFilter_byFileName() {
        filter.setFileName("name_1");
        List<TransportMessage> result = transportMessageDao.findByFilter(filter, pagingParams);
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .extracting("blob")
                .extracting("uuid")
                .containsExactly("uuid_1");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void test_create() {
        transportMessageDao.create(TransportMessage.builder()
                .dateTime(LocalDateTime.now())
                .messageUuid(UUID.randomUUID().toString().toLowerCase())
                .type(TransportMessageType.OUTGOING)
                .receiverSubsystem(Subsystem.builder().id(11).build())
                .contentType(TransportMessageContentType.ERROR_MESSAGE)
                .state(TransportMessageState.CONFIRMED)
                .initiatorUser(createUser())
                .build());
    }

    private TAUser createUser() {
        TAUser user = new TAUser();
        user.setId(1);
        return user;
    }
}
