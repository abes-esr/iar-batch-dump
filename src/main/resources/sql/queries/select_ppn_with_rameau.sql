-- Notices sélectionnées (ont un titre, un résumé, un sujet rameau, etc)
WITH notice_candidate AS (
    SELECT
        b.ppn,
        (select ppn from iar_biblio_table_frbr_1XX a where b.ppn=a.ppn and a.tag='105$a' and (substr(a.datas,4,4) like '%v%' or substr(a.datas,4,4) like '%m%' or substr(a.datas,4,4) like '%7%') and  rownum<2) as ppn_these
    FROM iar_biblio_table_generale b
    WHERE b.biblevel = 'a' AND b.typecontrol = 'm'
      AND EXISTS ( SELECT 1 FROM iar_biblio_table_lien_rameau r WHERE r.ppn = b.ppn )      -- La notice possède au moins un sujet RAMEAU
      AND EXISTS ( SELECT 1 FROM iar_biblio_table_frbr_3XX r WHERE r.ppn = b.ppn AND r.tag = '330$a' )      -- La notice possède au moins un résumé
      AND ROWNUM <= 1000
),

-- refiltre certaines notices, et récupère la position des zones rameau 606
notice_valide AS (
    SELECT DISTINCT
    b.ppn,
    b.posfield,
    c.ppn_these
FROM notice_candidate c
JOIN iar_biblio_table_frbr_6XX a ON a.ppn = c.ppn
JOIN iar_biblio_table_frbr_6XX b ON b.ppn = a.ppn AND b.posfield = a.posfield
WHERE a.tag = '606$2' AND a.datas = 'rameau'
  
  -- La zone 606 possède un identifiant RAMEAU ($3)
  AND b.tag = '606$3'
  
  AND EXISTS ( SELECT 1 FROM iar_aut_table_frbr_0xx g WHERE g.ppn = b.datas AND g.tag = '008$0' AND g.datas = 'Td8' )  -- Le concept RAMEAU associé est de type Td8
  AND NOT EXISTS ( SELECT 1 FROM iar_aut_table_frbr_9XX f WHERE f.ppn = b.datas AND f.tag = '950$a' AND f.datas LIKE '%--%' )   -- Le concept n'est pas un RAMEAU imbriqué (?)
),


-- concatène les sujets rameau au sein d'une même zone 606
rameau_par_zone AS (
    SELECT
        zz.ppn,
        zz.posfield,
        LISTAGG( zz2.datas || '#' || zz.datas, ' -- ' ) WITHIN GROUP (ORDER BY zz.ppn) AS rameau
    FROM notice_valide v
    JOIN iar_biblio_table_frbr_extend zz ON zz.ppn = v.ppn AND zz.posfield = v.posfield AND zz.tag IN ('606$a', '606$x')
    JOIN iar_biblio_table_frbr_extend zz2 ON zz2.ppn = v.ppn AND zz2.posfield = v.posfield AND zz.POSSUBFIELD - 1 = zz2.POSSUBFIELD AND zz2.tag = '606$3'
    GROUP BY zz.ppn, zz.posfield
),
-- concatène les zones rameau d'une même notice
rameau_par_notice AS (
    SELECT
        ppn,
        LISTAGG( rameau, ';' ) WITHIN GROUP (ORDER BY ppn) AS rameau
    FROM rameau_par_zone
    GROUP BY ppn
)

SELECT
    n.ppn,
    n.ppn_these,
    -- Titre + sous-titre
    (
        SELECT LISTAGG( REPLACE(REPLACE(a.datas, '?', ''), '?', ''), ' : ' ON OVERFLOW TRUNCATE)  WITHIN GROUP ( ORDER BY a.posfield, a.possubfield )
        FROM iar_biblio_table_frbr_2XX a
        WHERE a.ppn = n.ppn AND a.tag IN ('200$a', '200$e') AND ROWNUM < 15
    ) AS titre,
    
    -- Résumé
    (
        SELECT RTRIM( XMLAGG( XMLELEMENT( e, b.datas, ' ____ ' ).EXTRACT('//text()') ORDER BY b.posfield, b.possubfield ).GETCLOBVAL(), ': ' )
        FROM iar_biblio_table_frbr_3XX b
        WHERE b.ppn = n.ppn
          AND b.tag = '330$a'
    ) AS resume,
    
    -- Langues
    (
        SELECT LISTAGG( c.datas, ' : ' ) WITHIN GROUP ( ORDER BY c.posfield, c.possubfield )
        FROM iar_biblio_table_frbr_1XX c
        WHERE c.ppn = n.ppn
          AND c.tag = '101$a'
    ) AS langue,
    
    -- RAMEAU concaténés
    r.rameau

FROM (
    SELECT DISTINCT
        ppn,
        ppn_these
    FROM notice_valide
) n

LEFT JOIN rameau_par_notice r
    ON r.ppn = n.ppn;
