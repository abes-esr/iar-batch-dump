package com.levant.iar.batch.reader;

import com.levant.iar.batch.model.PpnData;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for PpnReaderConfig to verify that the reader can read PPN data.
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/test-data.sql")
class PpnReaderConfigTest {

    @Autowired
    private JdbcCursorItemReader<PpnData> ppnItemReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testPpnItemReader_ReadsData() throws Exception {
        // Reset the reader for test
        ppnItemReader.open(jdbcTemplate.getDataSource().getConnection());
        
        List<PpnData> items = new ArrayList<>();
        PpnData item;
        
        while ((item = ppnItemReader.read()) != null) {
            items.add(item);
        }
        
        // Should have read test data
        assertThat(items).isNotEmpty();
        
        // Verify first item
        PpnData firstItem = items.get(0);
        assertThat(firstItem.getPpn()).isNotNull();
        assertThat(firstItem.getTitre()).isNotNull();
        
        ppnItemReader.close();
    }
}
