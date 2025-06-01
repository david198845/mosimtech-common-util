package de.mosimtech.common.jpa.annotaions

import de.mosimtech.common.jpa.repository.impl.UrnCrudRepositoryImpl
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


/**
 * Annotation to enable JPA repositories with custom base class functionality.
 *
 * This annotation is used to enable custom repository behavior for JPA repositories that
 * extend from the `UrnCrudRepository` interface. It configures the JPA repositories to use
 * the custom repository implementation provided by `UrnCrudRepositoryImpl` as the base class.
 *
 * Usage:
 * Placed on a configuration class to enable scanning of Spring Data JPA repositories
 * that support additional custom functionality tailored to URN-based identifier handling.
 *
 * Targets:
 * - Classes annotated with this annotation will have their repositories configured with
 *   custom behavior for URN handling and namespace-based operations.
 *
 * Retention:
 * - Retained at runtime to allow Spring's annotation processing to properly configure the repositories.
 *
 * Annotations:
 * - `@EnableJpaRepositories`: Configures the Spring Data JPA functionality to leverage the custom
 *   repository base class (`UrnCrudRepositoryImpl`) for repositories.
 *
 * Attributes set in `@EnableJpaRepositories`:
 * - `repositoryBaseClass`: Specifies the custom implementation class `UrnCrudRepositoryImpl` to be used
 *   as the default base class for repository interfaces extending `UrnCrudRepository`.
 */
@Target(AnnotationTarget.CLASS)
// Retention annotation for runtime
@Retention(AnnotationRetention.RUNTIME)
// Annotation to enable JPA repositories with a custom base class
@EnableJpaRepositories(repositoryBaseClass = UrnCrudRepositoryImpl::class)
annotation class EnableUrnRepositories
