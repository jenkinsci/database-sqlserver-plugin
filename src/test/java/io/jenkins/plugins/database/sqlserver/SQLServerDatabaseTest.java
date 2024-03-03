package io.jenkins.plugins.database.sqlserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import hudson.util.Secret;
import java.io.IOException;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers(disabledWithoutDocker = true)
public class SQLServerDatabaseTest {

    public static final String TEST_IMAGE = "mcr.microsoft.com/mssql/server:2022-latest";

    @Container
    private static final MSSQLServerContainer<?> sqlserver = new MSSQLServerContainer<>(TEST_IMAGE).acceptLicense();

    public void setConfiguration() throws IOException {
        SQLServerDatabase database = new SQLServerDatabase(
                sqlserver.getHost() + ":" + sqlserver.getMappedPort(1433),
                "master",
                sqlserver.getUsername(),
                Secret.fromString(sqlserver.getPassword()),
                null);
        database.setValidationQuery("SELECT 1");
        GlobalDatabaseConfiguration.get().setDatabase(database);
    }

    @Test
    public void shouldSetConfiguration(JenkinsRule j) throws IOException {
        setConfiguration();
        assertThat(GlobalDatabaseConfiguration.get().getDatabase(), instanceOf(SQLServerDatabase.class));
    }

    @Test
    public void shouldConstructDatabase(JenkinsRule j) throws IOException {
        SQLServerDatabase database = new SQLServerDatabase(
                sqlserver.getHost() + ":" + sqlserver.getMappedPort(1433),
                "master",
                sqlserver.getUsername(),
                Secret.fromString(sqlserver.getPassword()),
                null);
        assertThat(database.getDescriptor().getDisplayName(), is("Microsoft SQL Server"));
        assertThat(
                database.getJdbcUrl(),
                is("jdbc:sqlserver://" + sqlserver.getHost() + ":" + sqlserver.getMappedPort(1433) + ";databaseName="
                        + "master"));
        assertThat(database.getDriverClass(), is(com.microsoft.sqlserver.jdbc.SQLServerDriver.class));
    }
}
