package mr.bcm.gestionprets.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PlanAmortissementResponse(
        Long materielId,
        String numeroInventaire,
        String designation,
        BigDecimal prixAchat,
        LocalDate dateAchat,
        int dureeAmortissementAnnees,
        BigDecimal annuite,
        List<LigneAmortissementResponse> lignes) {
}
