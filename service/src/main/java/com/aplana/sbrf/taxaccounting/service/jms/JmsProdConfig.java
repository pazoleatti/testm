package com.aplana.sbrf.taxaccounting.service.jms;

import com.aplana.sbrf.taxaccounting.AndProfileCondition;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.BeanFactoryDestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Configuration
@Import(JmsBaseConfig.class)
@Conditional(AndProfileCondition.class)
@Profile({"production", "jms"})
@EnableJms
public class JmsProdConfig {

    @Bean
    public BeanFactoryDestinationResolver destinationResolver(BeanFactory beanFactory) {
        return new BeanFactoryDestinationResolver(beanFactory);
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory,
                                   MessageConverter messageConverter,
                                   BeanFactoryDestinationResolver destinationResolver) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setMessageConverter(messageConverter);
        jmsTemplate.setDestinationResolver(destinationResolver);
        return jmsTemplate;
    }


    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                      MessageConverter messageConverter,
                                                                      BeanFactoryDestinationResolver destinationResolver) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(destinationResolver);
        factory.setSessionTransacted(true);
        factory.setMaxMessagesPerTask(1);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    /**
     * Фабрика соединений с MQ для очередей, получается по JNDI у вебсферы.
     */
    @Bean(name = "fundConnectionFactory")
    public ConnectionFactory connectionFactory() throws NamingException {
        return (ConnectionFactory) new InitialContext()
                .lookup("java:comp/env/jms/FundConnectionFactory");
    }

    /**
     * Очередь входящих запросов, получается по JNDI у вебсферы.
     */
    @Bean(name = JmsBaseConfig.FROM_NDFL_QUEUE)
    public Destination fundFromNdflSubsystemQueue() throws NamingException {
        return (Destination) new InitialContext()
                .lookup("java:comp/env/jms/FundFromNdflSubsystemQueue");
    }

    /**
     * Очередь исходящих запросо, получается по JNDI у вебсферы.
     */
    @Bean(name = JmsBaseConfig.TO_NDFL_QUEUE)
    public Destination fundToNdflSubsystemQueue() throws NamingException {
        return (Destination) new InitialContext()
                .lookup("java:comp/env/jms/FundToNdflSubsystemQueue");
    }

    /**
     * Вспомогательный класс для работы с JMS, в промышленном профиле нужно явно создать его на основе бинов сферы.
     */
    @Bean
    public JmsTemplate edoJmsTemplate(ConnectionFactory edoConnectionFactory,
                                      MessageConverter messageConverter,
                                      BeanFactoryDestinationResolver destinationResolver) {

        JmsTemplate jmsTemplate = new JmsTemplate(edoConnectionFactory);
        jmsTemplate.setMessageConverter(messageConverter);
        jmsTemplate.setDestinationResolver(destinationResolver);
        return jmsTemplate;
    }

    @Bean
    public JmsListenerContainerFactory<?> edoJmsListenerContainerFactory(ConnectionFactory edoConnectionFactory,
                                                                         MessageConverter messageConverter,
                                                                          BeanFactoryDestinationResolver destinationResolver,
                                                                         PlatformTransactionManager transactionManager) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(edoConnectionFactory);
        factory.setDestinationResolver(destinationResolver);
        factory.setTransactionManager(transactionManager);
        factory.setSessionTransacted(true);
        // Обработчик будет убиваться после обработчки одного запроса, чтобы не держать конекты к БД
        factory.setMaxMessagesPerTask(1);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
