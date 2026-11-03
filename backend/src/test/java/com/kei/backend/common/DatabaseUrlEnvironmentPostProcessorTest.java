package com.kei.backend.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();

    @Test
    void convertsNeonUrlToJdbcAndUsesDirectHost() {
        Map<String, Object> props = DatabaseUrlEnvironmentPostProcessor.toDatasourceProperties(
                "postgresql://neondb_owner:s3cr%40t@ep-cool-river-123-pooler.ap-southeast-1.aws.neon.tech/neondb"
                        + "?sslmode=require&channel_binding=require");

        assertThat(props)
                .containsEntry("spring.datasource.url",
                        "jdbc:postgresql://ep-cool-river-123.ap-southeast-1.aws.neon.tech/neondb?sslmode=require")
                .containsEntry("spring.datasource.username", "neondb_owner")
                .containsEntry("spring.datasource.password", "s3cr@t");
    }

    @Test
    void keepsPortAndWorksWithoutQuery() {
        Map<String, Object> props = DatabaseUrlEnvironmentPostProcessor.toDatasourceProperties(
                "postgres://app:pw@db.example.com:6543/expenditure");

        assertThat(props).containsEntry("spring.datasource.url", "jdbc:postgresql://db.example.com:6543/expenditure");
    }

    @Test
    void prefersUnpooledUrlAndLeavesExplicitDbUrlAlone() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://u:p@pooled-pooler.neon.tech/db")
                .withProperty("DATABASE_URL_UNPOOLED", "postgresql://u:p@direct.neon.tech/db");
        processor.postProcessEnvironment(env, null);
        assertThat(env.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://direct.neon.tech/db");

        MockEnvironment explicit = new MockEnvironment()
                .withProperty("DB_URL", "jdbc:postgresql://localhost:5434/expenditure")
                .withProperty("DATABASE_URL", "postgresql://u:p@other.neon.tech/db");
        processor.postProcessEnvironment(explicit, null);
        assertThat(explicit.getProperty("spring.datasource.url")).isNull();
    }

    @Test
    void rejectsNonPostgresUrl() {
        assertThatThrownBy(() -> DatabaseUrlEnvironmentPostProcessor.toDatasourceProperties("mysql://u:p@h/db"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
