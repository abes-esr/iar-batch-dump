#!/bin/bash

# Script pour lancer le test BatchConfigReaderTest
# Ce script nécessite Maven d'être installé

echo "=========================================="
echo "Lancement du test BatchConfigReaderTest"
echo "=========================================="

# Vérifier que Maven est installé
if ! command -v mvn &> /dev/null; then
    echo "ERREUR : Maven n'est pas installé."
    echo "Pour installer Maven :"
    echo "  - Sur Windows avec Chocolatey : choco install maven"
    echo "  - Sur Mac : brew install maven"
    echo "  - Sur Linux : sudo apt-get install maven"
    exit 1
fi

echo ""
echo "✓ Maven est installé"

# Lancer le test spécifique
echo ""
echo "Lancement du test : BatchConfigReaderTest.testReader_ConnectsAndReadsData"
mvn clean test \
    -Dtest=BatchConfigReaderTest \
    -Dspring.profiles.active=test \
    -q

# Vérifier le résultat
if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✓ TEST REUSSI !"
    echo "=========================================="
else
    echo ""
    echo "=========================================="
    echo "✗ TEST ECHOUE"
    echo "=========================================="
fi

echo ""
echo "Pour voir les détails des tests :"
echo "  mvn test -Dtest=BatchConfigReaderTest"
