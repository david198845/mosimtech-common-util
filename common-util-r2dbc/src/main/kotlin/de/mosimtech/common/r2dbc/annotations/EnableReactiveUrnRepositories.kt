package de.mosimtech.common.r2dbc.annotations

import de.mosimtech.common.r2dbc.repository.impl.UrnReactiveCrudRepositoryImpl
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

/**
 * Annotation to enable R2DBC repositories with custom base class functionality.
 *
 * This annotation is used to enable custom repository behavior for R2DBC repositories that
 * extend from the `UrnReactiveCrudRepository` interface. It configures the R2DBC repositories to use
 * the custom repository implementation provided by `UrnReactiveCrudRepositoryImpl` as the base class.
 *
 * Usage:
 * Placed on a configuration class to enable scanning of Spring Data R2DBC repositories
 * that support additional custom functionality tailored to URN-based identifier handling.
 *
 * Targets:
 * - Classes annotated with this annotation will have their repositories configured with
 *   custom behavior for URN handling and namespace-based operations.
 *
 * Retention:
 * - Retained at runtime to allow Spring's annotation processing to properly configure the repositories.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@EnableR2dbcRepositories(repositoryBaseClass = UrnReactiveCrudRepositoryImpl::class)
annotation class EnableReactiveUrnRepositories
