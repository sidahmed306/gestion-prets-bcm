package mr.bcm.gestionprets.dto;

import java.time.LocalDate;

import mr.bcm.gestionprets.entity.Pret;
import mr.bcm.gestionprets.entity.StatutPret;

public record PretResponse(
        Long id,
        MaterielResponse materiel,
        EmployeResponse employe,
        LocalDate datePret,
        LocalDate dateRetourPrevue,
        LocalDate dateRetourEffective,
        StatutPret statut,
        String commentaire,
        boolean enRetard) {

    public static PretResponse of(Pret pret) {
        return new PretResponse(
                pret.getId(),
                MaterielResponse.of(pret.getMateriel()),
                EmployeResponse.of(pret.getEmploye()),
                pret.getDatePret(),
                pret.getDateRetourPrevue(),
                pret.getDateRetourEffective(),
                pret.getStatut(),
                pret.getCommentaire(),
                pret.isEnRetard());
    }
}
