package mr.bcm.gestionprets.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PretRequest(
        @NotNull Long materielId,
        @NotNull Long employeId,
        @NotNull LocalDate datePret,
        @NotNull LocalDate dateRetourPrevue,
        String commentaire) {
}
