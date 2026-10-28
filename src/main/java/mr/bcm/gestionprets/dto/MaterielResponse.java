package mr.bcm.gestionprets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import mr.bcm.gestionprets.comptabilite.CalculAmortissement;
import mr.bcm.gestionprets.entity.Materiel;

/**
 * @param situationComptable valeur actuelle calculée à la date du jour ;
 *                           null si le prix, la date d'achat ou la durée ne sont pas renseignés
 */
public record MaterielResponse(
        Long id,
        String numeroInventaire,
        String designation,
        CategorieResponse categorie,
        String marque,
        String modele,
        String etatPhysique,
        BigDecimal prixAchat,
        LocalDate dateAchat,
        Integer dureeAmortissementAnnees,
        String fournisseur,
        SituationComptableResponse situationComptable) {

    public static MaterielResponse of(Materiel materiel) {
        SituationComptableResponse situation = materiel.isValorise()
                ? CalculAmortissement.situation(materiel.getPrixAchat(), materiel.getDateAchat(),
                        materiel.getDureeAmortissementAnnees(), LocalDate.now())
                : null;
        return new MaterielResponse(
                materiel.getId(),
                materiel.getNumeroInventaire(),
                materiel.getDesignation(),
                CategorieResponse.of(materiel.getCategorie()),
                materiel.getMarque(),
                materiel.getModele(),
                materiel.getEtatPhysique(),
                materiel.getPrixAchat(),
                materiel.getDateAchat(),
                materiel.getDureeAmortissementAnnees(),
                materiel.getFournisseur(),
                situation);
    }
}
