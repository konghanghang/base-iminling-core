package com.iminling.core.config.jpa

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import javax.persistence.EntityManager

@ConditionalOnClass(JPAQueryFactory::class)
// @AutoConfigureBefore(JpaRepositoriesAutoConfiguration::class)
class CustomizeJpaConfiguration {

    @Bean
    fun jpaQueryFactory(entityManager: EntityManager): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }

}