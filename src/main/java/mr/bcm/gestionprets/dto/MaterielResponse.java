package mr.bcm.gestionprets.dto;

import mr.bcm.gestionprets.entity.Materiel;

public record MaterielResponse(
        Long id,
        String numeroInventaire,
        String designation,
        CategorieResponse categorie,
        String marque,
        String modele,
        String etatPhysique) {

    public static MaterielResponse of(Materiel materiel) {
        return new MaterielResponse(
                materiel.getId(),
                materiel.getNumeroInventaire(),
                materiel.getDesignation(),
                CategorieResponse.of(materiel.getCategorie()),
                materiel.getMarque(),
                materiel.getModele(),
                materiel.getEtatPhysique());
    }
}
