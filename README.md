# Architecture du batch

flowchart TD
subgraph Extraction["Job 1: RameauExtractJob"]
A[Reader: PPN depuis BIBLIO_TABLE_GENERALE] --> B[Processor: Enrichissement Rameau/Thèse/Titre]
B --> C[Writer: Écriture CSV]
end

    subgraph Upload["Job 2: RameauUploadJob"]
        D[Tasklet: Upload fichier via HTTP]
    end

    subgraph Vectorization["Job 3: RameauVectorizationJob"]
        E[Tasklet: Appel API vectorisation]
    end

    Extraction -->|fichier CSV| Upload
    Upload -->|fichier uploadé| Vectorization