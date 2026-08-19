# Configuration du Projet IAR Batch Dump

## Prérequis

### 1. Java JDK 17
Ce projet nécessite **Java 17** pour fonctionner correctement avec Spring Boot 3.2.x.

#### Installation JDK 17

**Sur Windows avec Chocolatey :**
```powershell
choco install openjdk17
```

**Sur Mac avec Homebrew :**
```bash
brew install openjdk@17
```

**Sur Linux (Ubuntu/Debian) :**
```bash
sudo apt-get install openjdk-17-jdk
```

#### Configuration dans IntelliJ IDEA

1. **File → Settings → Build, Execution, Deployment → Build Tools → Maven**
   - **JDK for importer** : Sélectionner JDK 17
   - **Runner** : Sélectionner JDK 17

2. **File → Project Structure**
   - **Project SDK** : Sélectionner JDK 17
   - **Project language level** : 17

3. **Vérifier la configuration**
   ```bash
   java -version
   # Doit afficher : openjdk version "17"...
   ```

---

## 2. Maven

**Version requise :** Maven 3.8+

**Vérification :**
```bash
mvn -v
# Doit afficher : Apache Maven 3.8.x ou supérieur
```

**Installation Maven :**
- Windows (Chocolatey) : `choco install maven`
- Mac (Homebrew) : `brew install maven`
- Linux : `sudo apt-get install maven`

---

## 3. Configuration IDE

### IntelliJ IDEA

1. **Importer le projet :**
   - File → Open → Sélectionner le répertoire du projet
   - IntelliJ détectera automatiquement le pom.xml

2. **Activer Lombok :**
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Cocher **Enable annotation processing**
   - Installer le plugin Lombok si nécessaire

3. **Configurer le encoding :**
   - File → Settings → Editor → File Encodings
   - Définir **UTF-8** pour Project Encoding et Default encoding for properties files

---

## 4. Base de données

### Pour le développement
Le projet utilise **H2 Database** en mémoire. Aucune installation nécessaire.

### Pour la production
Configurer les propriétés Oracle dans `application.yml` :
```yaml
datasource:
  oracle:
    url: jdbc:oracle:thin:@//votre-serveur:1521/VOTRE_SERVICE
    username: votre_utilisateur
    password: votre_mot_de_passe
```

---

## 5. Exécution

### Démarrer l'application
```bash
mvn spring-boot:run
```

### Exécuter les tests
```bash
mvn clean test
```

### Exécuter un test spécifique
```bash
mvn test -Dtest=BatchConfigReaderTest
```

---

## 6. Résolution des problèmes

### Erreur : "ClassCircularityError"
**Cause :** Java 19 utilisé au lieu de Java 17

**Solution :**
1. Vérifier la version Java : `java -version`
2. Configurer IntelliJ pour utiliser JDK 17 (voir section 1)
3. Nettoyer et reconstruire : `mvn clean install`

### Erreur : "Bean definition overriding"
**Cause :** Plusieurs définitions du même bean

**Solution :**
```bash
mvn clean
# Vérifier qu'il n'y a pas de fichiers dupliqués
```

### Erreur : "Table not found" dans les tests
**Cause :** Le script SQL de test n'a pas été exécuté

**Solution :**
- Vérifier que `@Sql(scripts = "/sql/test-data.sql")` est présent sur le test
- Vérifier que le fichier `src/test/resources/sql/test-data.sql` existe

---

## 7. Structure du projet

```
iar-batch-dump/
├── src/
│   ├── main/
│   │   ├── java/com/levant/iar/batch/
│   │   │   ├── config/              # Configurations Spring
│   │   │   │   ├── BatchConfig.java    # Configuration des jobs
│   │   │   │   ├── OracleDataSourceConfig.java
│   │   │   │   └── AppProperties.java
│   │   │   ├── model/               # Modèles
│   │   │   │   └── PpnData.java
│   │   │   └── IarBatchApplication.java
│   │   └── resources/
│   │       ├── application.yml       # Configuration principale
│   │       └── sql/queries/
│   │           └── select_ppn_with_rameau.sql
│   └── test/
│       ├── java/com/levant/iar/batch/config/
│       │   └── BatchConfigReaderTest.java  # Test du reader
│       └── resources/
│           ├── application-test.yml
│           └── sql/
│               ├── test-data.sql
│               └── queries/
│                   └── select_ppn_with_rameau.sql
├── pom.xml
└── README.md
```

---

## 8. Variables d'environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|------------------|
| ORACLE_URL | URL de la base Oracle | jdbc:oracle:thin:@//localhost:1521/ORCLPDB1 |
| ORACLE_USERNAME | Utilisateur Oracle | system |
| ORACLE_PASSWORD | Mot de passe Oracle | oracle |

---

## 9. Support

Pour toute question ou problème, vérifier :
1. Ce fichier de configuration
2. Les logs de Maven (`mvn -X ...`)
3. La documentation Spring Boot : https://spring.io/projects/spring-boot
