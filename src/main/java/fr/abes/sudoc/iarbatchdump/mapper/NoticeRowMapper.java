package fr.abes.sudoc.iarbatchdump.mapper;

import org.springframework.jdbc.core.RowMapper;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NoticeRowMapper
        implements RowMapper<CsvRecord> {

    @Override
    public CsvRecord mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        
        return CsvRecord.builder()
                        .ppn(rs.getString("PPN"))
                        .these(rs.getString("PPN_THESE"))
                        .titre(rs.getString("TITRE"))
                        .resume(rs.getString("RESUME"))
                        .langue(rs.getString("LANGUE"))
                        .libelleRameau(rs.getString("RAMEAU"))
                        .build();
        
    }
}