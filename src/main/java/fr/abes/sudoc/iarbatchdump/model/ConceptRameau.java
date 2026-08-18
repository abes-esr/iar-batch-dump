package fr.abes.sudoc.iarbatchdump.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConceptRameau {
    private int ppn;
    private String libelle;
}
