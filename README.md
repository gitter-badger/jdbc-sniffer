JDBC Sniffer
============
[![CI Status](https://travis-ci.org/bedrin/jdbc-sniffer.svg?branch=develop)](https://travis-ci.org/bedrin/jdbc-sniffer)
[![Coverage Status](https://coveralls.io/repos/bedrin/jdbc-sniffer/badge.png?branch=master)](https://coveralls.io/r/bedrin/jdbc-sniffer?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bedrin/jdbc-sniffer/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.bedrin/jdbc-sniffer)

JDBC Sniffer counts the number of executed SQL queries and provides an API for validating it
It is very useful in unit tests and allows you to test if particular method doesn't make more than N SQL queries

Maven
============
JDBC Sniffer is available from Maven Central repository
```xml
<dependency>
    <groupId>com.github.bedrin</groupId>
    <artifactId>jdbc-sniffer</artifactId>
    <version>1.4</version>
</dependency>
```

For Gradle users:
```javascript
dependencies {
    compile 'com.github.bedrin:jdbc-sniffer:1.4'
}
```

Download
============
- [jdbc-sniffer-1.4.jar](https://github.com/bedrin/jdbc-sniffer/releases/download/1.4/jdbc-sniffer-1.1.jar)
- [jdbc-sniffer-1.4-sources.jar](https://github.com/bedrin/jdbc-sniffer/releases/download/1.4/jdbc-sniffer-1.1-sources.jar)
- [jdbc-sniffer-1.4-javadoc.jar](https://github.com/bedrin/jdbc-sniffer/releases/download/1.4/jdbc-sniffer-1.1-javadoc.jar)

Setup
============
Simply add jdbc-sniffer.jar to your classpath and add `sniffer:` prefix to the JDBC connection url
For example `jdbc:h2:~/test` should be changed to `sniffer:jdbc:h2:~/test`
The sniffer JDBC driver class name is `com.github.bedrin.jdbc.sniffer.MockDriver`

JUnit Integration
============
JDBC Sniffer supports integration with JUnit framework via `@Rule`

Add a `QueryCounter` rule to your test and assert the maximum number of queries allowed for particular test using `@AllowedQueries(n)` and `@NotAllowedQueries` annotations

```java
package com.github.bedrin.jdbc.sniffer.junit;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class QueryCounterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public QueryCounter queryCounter = new QueryCounter();

    @BeforeClass
    public static void loadDriver() throws ClassNotFoundException {
        Class.forName("com.github.bedrin.jdbc.sniffer.MockDriver");
    }

    @Test
    @AllowedQueries(1)
    public void testAllowedOneQuery() throws SQLException {
        Connection connection = DriverManager.getConnection("sniffer:jdbc:h2:~/test", "sa", "sa");
        connection.createStatement().execute("SELECT 1 FROM DUAL");
    }

    @Test
    @NotAllowedQueries
    public void testNotAllowedQueries() throws SQLException {
        Connection connection = DriverManager.getConnection("sniffer:jdbc:h2:~/test", "sa", "sa");
        connection.createStatement().execute("SELECT 1 FROM DUAL");
        thrown.expect(AssertionError.class);
    }

}
```

Sniffer API
============
The number of executed queries is available via static methods of several classes:
`com.github.bedrin.jdbc.sniffer.Sniffer`, `com.github.bedrin.jdbc.sniffer.ThreadLocalSniffer` and `com.github.bedrin.jdbc.sniffer.OtherThreadsSniffer`

First one holds the number of SQL queries executed by all threads, second one holds the number of SQL queries generated by current thread only, and the last one counts SQL queries executed by all threads except for the current one

```java
@Test
public void testExecuteStatement() throws ClassNotFoundException, SQLException {
    // Just add sniffer: in front of your JDBC connection URL in order to enable sniffer
    Connection connection = DriverManager.getConnection("sniffer:jdbc:h2:~/test", "sa", "sa");
    // Sniffer.reset() sets the internal counter of queries to zero
    Sniffer.reset();
    // You do not need to modify your JDBC code
    connection.createStatement().execute("SELECT 1 FROM DUAL");
    // Sniffer.executedStatements() returns count of execute queries
    assertEquals(1, Sniffer.executedStatements());
    // Sniffer.verifyNotMoreThanOne() throws an AssertionError if more than one query was executed;
    // it also resets the counter to 0
    Sniffer.verifyNotMoreThanOne();
    // Sniffer.verifyNotMore() throws an AssertionError if any query was executed
    Sniffer.verifyNotMore();
}
```

If you want to count the number of queries generated by a particular block of code, JDBC Sniffer provides a convenient functional API:
```java
@Test
public void testFunctionalApi() throws SQLException {
    final Connection connection = DriverManager.getConnection("sniffer:jdbc:h2:~/test", "sa", "sa");
    // Sniffer.execute() method executes the lambda expression and returns an instance of RecordedQueries
    // this class provides methods for validating the number of executed queries
    Sniffer.execute(() -> connection.createStatement().execute("SELECT 1 FROM DUAL")).verifyNotMoreThanOne();
}
```


Building
============
JDBC sniffer is built using JDK8+ and Maven 3+ - just checkout the project and type `mvn install`
JDK8 is required only for building the project - once it's built, you can use JBC sniffer with JRE 1.6+