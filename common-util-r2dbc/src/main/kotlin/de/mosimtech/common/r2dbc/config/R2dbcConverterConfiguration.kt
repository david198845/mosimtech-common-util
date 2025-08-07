package de.mosimtech.common.r2dbc.config

import de.mosimtech.common.r2dbc.converter.UrnReadingConverter
import de.mosimtech.common.r2dbc.converter.UrnWritingConverter
import de.mosimtech.common.r2dbc.converter.YearMonthReadingConverter
import de.mosimtech.common.r2dbc.converter.YearMonthWritingConverter
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.dialect.R2dbcDialect
import org.springframework.r2dbc.core.DatabaseClient

/**
 * Configuration class for registering custom R2DBC converters.
 * This class provides the necessary beans to register custom type converters for R2DBC.
 */
@Configuration
open class R2dbcConverterConfiguration : AbstractR2dbcConfiguration() {

    /**
     * Creates an R2dbcCustomConversions bean that registers all custom converters for R2DBC.
     *
     * @param databaseClient The database client to determine the dialect
     * @return A configured R2dbcCustomConversions bean with all custom converters
     */
    @Bean
    open fun r2dbcCustomConversions(databaseClient: DatabaseClient): R2dbcCustomConversions {
        val dialect: R2dbcDialect = DialectResolver.getDialect(databaseClient.connectionFactory)
        
        return R2dbcCustomConversions.of(
            dialect,
            listOf(
                // Register all custom converters
                UrnReadingConverter(),
                UrnWritingConverter(),
                YearMonthReadingConverter(),
                YearMonthWritingConverter()
            )
        )
    }

    override fun connectionFactory(): ConnectionFactory {
        TODO("Not yet implemented")
    }
}
