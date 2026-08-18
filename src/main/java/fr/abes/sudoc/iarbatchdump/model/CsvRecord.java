package fr.abes.sudoc.iarbatchdump.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvRecord {
    private String ppn;
    private String these;
    private String titre;
    private String resume;
    private String libelleRameau;
    private String langue;

    // Méthode pour formater en ligne CSV (séparateur = tabulation)
    public String toCsvLine() {
        return String.join("\t",
                ppn != null ? ppn : "",
                these != null ? these : "",
                titre != null ? titre.replaceAll("[\\t\\n\\r]", " ") : "",
                resume != null ? resume.replaceAll("[\\t\\n\\r]", " ") : "",
                libelleRameau != null ? libelleRameau.replaceAll("[\\t\\n\\r]", " ") : "",
                langue != null ? langue : ""
        );
    }
}