-- Test data setup for H2 database
-- This script creates test tables and inserts sample data for testing

-- Create test table (simplified version of the Oracle schema)
CREATE TABLE IF NOT EXISTS test_ppn_data (
    ppn VARCHAR(50) PRIMARY KEY,
    ppn_these VARCHAR(50),
    titre VARCHAR(1000),
    resume VARCHAR(2000),
    langue VARCHAR(100),
    rameau VARCHAR(2000)
);

-- Insert test data
INSERT INTO test_ppn_data (ppn, ppn_these, titre, resume, langue, rameau) VALUES
('PPN001', 'THESE001', 'Titre de test 1', 'Résumé de test 1', 'fr', 'Sujet Rameau 1'),
('PPN002', 'THESE002', 'Titre de test 2', 'Résumé de test 2', 'en', 'Sujet Rameau 2'),
('PPN003', NULL, 'Titre de test 3', 'Résumé de test 3', 'fr', 'Sujet Rameau 3');

-- For testing with the actual SQL query, create a view that matches the query structure
CREATE VIEW IF NOT EXISTS iar_biblio_table_generale AS
SELECT ppn, 'a' as biblevel, 'm' as typecontrol FROM test_ppn_data;

CREATE VIEW IF NOT EXISTS iar_biblio_table_lien_rameau AS
SELECT ppn FROM test_ppn_data;

CREATE VIEW IF NOT EXISTS iar_biblio_table_frbr_3XX AS
SELECT ppn, 1 as posfield, 1 as possubfield, '330$a' as tag, resume as datas FROM test_ppn_data;

CREATE VIEW IF NOT EXISTS iar_biblio_table_frbr_6XX AS
SELECT ppn, 1 as posfield, 1 as possubfield, '606$2' as tag, 'rameau' as datas FROM test_ppn_data
UNION ALL
SELECT ppn, 1 as posfield, 2 as possubfield, '606$3' as tag, 'RAMEAU_ID' as datas FROM test_ppn_data;

CREATE VIEW IF NOT EXISTS iar_biblio_table_frbr_2XX AS
SELECT ppn, 1 as posfield, 1 as possubfield, '200$a' as tag, titre as datas FROM test_ppn_data;

CREATE VIEW IF NOT EXISTS iar_biblio_table_frbr_1XX AS
SELECT ppn, 1 as posfield, 1 as possubfield, '101$a' as tag, langue as datas FROM test_ppn_data;

CREATE VIEW IF NOT EXISTS iar_aut_table_frbr_0xx AS
SELECT 'RAMEAU_ID' as ppn, 1 as posfield, 1 as possubfield, '008$0' as tag, 'Td8' as datas FROM test_ppn_data LIMIT 1;

CREATE VIEW IF NOT EXISTS iar_biblio_table_frbr_extend AS
SELECT ppn, 1 as posfield, 1 as possubfield, '606$a' as tag, titre as datas FROM test_ppn_data
UNION ALL
SELECT ppn, 1 as posfield, 2 as possubfield, '606$x' as tag, titre as datas FROM test_ppn_data;

CREATE VIEW IF NOT EXISTS iar_aut_table_frbr_9XX AS
SELECT 'RAMEAU_ID' as ppn, 1 as posfield, 1 as possubfield, '950$a' as tag, 'NOT_IMBRIQUE' as datas FROM test_ppn_data LIMIT 1;
