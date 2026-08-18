package fr.abes.sudoc.iarbatchdump.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RameauExportParams {
    private String exportAction;  // update, init, upload_update, upload_init, launch_vectorisation
    private int nbJours;          // Nombre de jours pour les mises à jour
    private String action;        // init, update, auto (pour la vectorisation)
    private String conceptsORchains; // concepts, chains
    private String aliasModel;    // allMin, distiluse, e5-large
    private String avecThese;     // only_mono, only_theses, with_theses
    private String outputFilePath; // Chemin du fichier CSV (ex: /globule_pastel_rocou)
}