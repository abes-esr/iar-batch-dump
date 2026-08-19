-- Test SQL query for H2 database (simplified version for testing)
SELECT 
    'PPN001' as ppn,
    'THESE001' as ppn_these,
    'Titre de test 1' as titre,
    'Résumé de test 1' as resume,
    'fr' as langue,
    'Sujet Rameau 1' as rameau

UNION ALL

SELECT 
    'PPN002' as ppn,
    'THESE002' as ppn_these,
    'Titre de test 2' as titre,
    'Résumé de test 2' as resume,
    'en' as langue,
    'Sujet Rameau 2' as rameau

UNION ALL

SELECT 
    'PPN003' as ppn,
    NULL as ppn_these,
    'Titre de test 3' as titre,
    'Résumé de test 3' as resume,
    'fr' as langue,
    'Sujet Rameau 3' as rameau;
