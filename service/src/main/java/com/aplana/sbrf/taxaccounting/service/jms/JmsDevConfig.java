package com.aplana.sbrf.taxaccounting.service.jms;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.BeanFactoryDestinationResolver;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;


@Configuration
@Import(JmsBaseConfig.class)
@Profile(value = "development")
@EnableJms
/**
 * Конфигурация Jms в режиме разработки. Сейчас не используется.
 */
public class JmsDevConfig {

    @Bean
    public BeanFactoryDestinationResolver destinationResolver(BeanFactory beanFactory) {
        return new BeanFactoryDestinationResolver(beanFactory);
    }

    @Bean
    public JmsTemplate jmsTemplate(BeanFactoryDestinationResolver destinationResolver) {

        JmsTemplate jmsTemplate = new JmsTemplate(new ConnectionFactory() {
            @Override
            public Connection createConnection() throws JMSException {
                return null;
            }

            @Override
            public Connection createConnection(String s, String s1) throws JMSException {
                return null;
            }

            @Override
            public JMSContext createContext() {
                return null;
            }

            @Override
            public JMSContext createContext(String s, String s1) {
                return null;
            }

            @Override
            public JMSContext createContext(String s, String s1, int i) {
                return null;
            }

            @Override
            public JMSContext createContext(int i) {
                return null;
            }
        });
        jmsTemplate.setDestinationResolver(destinationResolver);
        return jmsTemplate;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory() {

        return new DefaultJmsListenerContainerFactory();
}
    @Bean(name = JmsBaseConfig.FROM_NDFL_QUEUE)
    public Destination fundFromNdflSubsystemQueue() {
        return new ActiveMQQueue(JmsBaseConfig.FROM_NDFL_QUEUE);
    }

    @Bean(name = JmsBaseConfig.TO_NDFL_QUEUE)
    public Destination fundToNdflSubsystemQueue() {
        return new ActiveMQQueue(JmsBaseConfig.TO_NDFL_QUEUE);
    }

    @Bean
    public JmsListenerContainerFactory<?> edoJmsListenerContainerFactory() {

        return new DefaultJmsListenerContainerFactory();
    }

}
