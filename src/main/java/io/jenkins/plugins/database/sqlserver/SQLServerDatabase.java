package io.jenkins.plugins.database.sqlserver;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.Secret;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.database.AbstractRemoteDatabase;
import org.jenkinsci.plugins.database.AbstractRemoteDatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class SQLServerDatabase extends AbstractRemoteDatabase {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public SQLServerDatabase(String hostname, String database, String username, Secret password, String properties) {
        super(hostname, database, username, password, properties);
    }

    @Override
    protected Class<? extends Driver> getDriverClass() {
        return SQLServerDriver.class;
    }

    /**
     * @see <a href="https://docs.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver16">JDBC Connection URL</a>
     */
    @Override
    protected String getJdbcUrl() {
        return "jdbc:sqlserver://" + hostname + ";databaseName=" + database;
    }

    @Extension
    public static class DescriptorImpl extends AbstractRemoteDatabaseDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return "Microsoft SQL Server";
        }

        @POST
        public FormValidation doCheckProperties(@QueryParameter String properties) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);

            try {
                Set<String> validPropertyNames = new HashSet<String>();
                Properties props = Util.loadProperties(properties);
                for (DriverPropertyInfo p :
                        new SQLServerDriver().getPropertyInfo("jdbc:sqlserver://localhost;databaseName=dummy", props)) {
                    validPropertyNames.add(p.name);
                }

                for (Map.Entry<Object, Object> e : props.entrySet()) {
                    String key = e.getKey().toString();
                    if (!validPropertyNames.contains(key)) return FormValidation.error("Unrecognized property: " + key);
                }
                return FormValidation.ok();
            } catch (Throwable e) {
                return FormValidation.warning(e, "Failed to validate the connection properties");
            }
        }
    }
}
