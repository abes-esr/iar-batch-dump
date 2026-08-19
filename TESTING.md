# Testing Guide - IAR Batch Dump

## Prérequis

- Java 17+
- Maven 3.8+
- H2 Database (inclus avec Spring Boot)

## Configuration des tests

Les tests utilisent une base de données **H2 en mémoire** avec les configurations suivantes :

- **Fichier de configuration** : `src/test/resources/application-test.yml`
- **DataSource Oracle en test** : Configure pour pointer vers H2 (`jdbc:h2:mem:oracle_test`)
- **DataSource principale** : H2 (`jdbc:h2:mem:testdb`)

## Données de test

Les données de test sont créées via :
- **Script SQL** : `src/test/resources/sql/test-data.sql`
  - Crée la table `test_ppn_data`
  - Insère 3 enregistrements de test
  - Crée les vues nécessaires pour simuler la structure Oracle

- **Requête SQL** : `src/test/resources/sql/queries/select_ppn_with_rameau.sql`
  - Version simplifiée pour H2
  - Retourne directement les colonnes : ppn, ppn_these, titre, resume, langue, rameau

## Exécuter les tests

### Tester uniquement le reader

```bash
mvn test -Dtest=BatchConfigReaderTest -Dspring.profiles.active=test
```

### Tester tout le projet

```bash
mvn clean test
```

### Tester avec plus de logs

```bash
mvn test -Dtest=BatchConfigReaderTest -Dspring.profiles.active=test -X
```

## Contenu du test : BatchConfigReaderTest

Ce test vérifie que :

1. ✅ **Connexion à la base de données** - Le reader se connecte à la DataSource Oracle (H2 en test)
2. ✅ **Chargement de la requête SQL** - La requête est chargée depuis le fichier `select_ppn_with_rameau.sql`
3. ✅ **Exécution de la requête** - La requête est exécutée et retourne des résultats
4. ✅ **Récupération des données** - Les données sont correctement mappées vers des objets `PpnData`
5. ✅ **Validation des résultats** - Les données lues contiennent les champs attendus (ppn, titre, etc.)

## Résolution des problèmes

### Erreur : "Table not found"

**Solution** : Vérifiez que le script `test-data.sql` est exécuté avant le test. Le test utilise `@Sql(scripts = "/sql/test-data.sql")` pour créer les tables.

### Erreur : "Column not found"

**Solution** : Vérifiez que la requête SQL dans `select_ppn_with_rameau.sql` utilise les bons noms de colonnes. En test, utilisez la version simplifiée fournie.

### Erreur : "DataSource not configured"

**Solution** : Vérifiez que `application-test.yml` contient bien la configuration de la DataSource H2.

## Structure des fichiers de test

```
src/test/
├── java/
│   └── com/levant/iar/batch/
│       ├── IarBatchApplicationTests.java  # Test de base Spring Boot
│       └── config/
│           └── BatchConfigReaderTest.java  # Test du reader
└── resources/
    ├── application-test.yml              # Configuration H2
    └── sql/
        ├── test-data.sql                  # Données de test
        └── queries/
            └── select_ppn_with_rameau.sql # Requête SQL simplifiée
```

## Exemple de sortie réussie

```
[INFO] Running com.levant.iar.batch.config.BatchConfigReaderTest
✓ Test réussi : 3 enregistrements lus
✓ Premier PPN : PPN001
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

## Debugging

Pour activer le mode debug et voir les requêtes SQL :

```bash
mvn test -Dtest=BatchConfigReaderTest -Dspring.profiles.active=test -Dlogging.level.org.hibernate.SQL=DEBUG -Dlogging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

Cela affichera :
- Les requêtes SQL exécutées
- Les paramètres passés
- Les résultats retournés
