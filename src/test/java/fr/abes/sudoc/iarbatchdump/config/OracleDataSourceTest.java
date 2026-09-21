package fr.abes.sudoc.iarbatchdump.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class OracleDataSourceTest {

    @Autowired
    @Qualifier("oracleDataSource")
    private DataSource oracleDataSource;

    @Test
    void testOracleConnection() throws SQLException {
        assertNotNull(oracleDataSource, "La DataSource Oracle doit être injectée.");

        try (Connection connection = oracleDataSource.getConnection()) {
            assertNotNull(connection, "La connexion à Oracle doit réussir.");
            System.out.println("Connexion à Oracle réussie !");
        }
    }
}