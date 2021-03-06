package com.github.bedrin.jdbc.sniffer.junit;

import com.github.bedrin.jdbc.sniffer.WrongNumberOfQueriesError;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class InheritSuperClassAnnotationTest extends BasedNoQueriesAllowedTest {

    @BeforeClass
    public static void loadDriver() throws ClassNotFoundException {
        Class.forName("com.github.bedrin.jdbc.sniffer.MockDriver");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public QueryCounter queryCounter = new QueryCounter();

    @Test
    public void testNoQueriesAllowedBySuperTest() {
        try {
            try (Connection connection = DriverManager.getConnection("sniffer:jdbc:h2:mem:", "sa", "sa");
                 Statement statement = connection.createStatement()) {
                statement.execute("SELECT 1 FROM DUAL");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        thrown.expect(WrongNumberOfQueriesError.class);
    }

}
