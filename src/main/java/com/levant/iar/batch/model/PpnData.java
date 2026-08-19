package com.levant.iar.batch.model;

import lombok.Data;

/**
 * DTO for PPN (Point d'accès Public Normalisé) data extracted from Oracle.
 * Contains bibliographic information including Rameau subjects.
 */
@Data
public class PpnData {

    /**
     * The PPN identifier of the bibliographic record.
     */
    private String ppn;

    /**
     * The PPN of the related thesis, if any.
     */
    private String ppnThese;

    /**
     * Title and subtitle of the document.
     */
    private String titre;

    /**
     * Abstract or summary of the document.
     */
    private String resume;

    /**
     * Languages of the document.
     */
    private String langue;

    /**
     * Rameau subjects concatenated.
     */
    private String rameau;

    @Override
    public String toString() {
        return "PpnData{" +
                "ppn='" + ppn + '\'' +
                ", ppnThese='" + ppnThese + '\'' +
                ", titre='" + titre + '\'' +
                ", resume='" + (resume != null ? resume.substring(0, Math.min(resume.length(), 100)) + "..." : null) + '\'' +
                ", langue='" + langue + '\'' +
                ", rameau='" + rameau + '\'' +
                '}';
    }
}
