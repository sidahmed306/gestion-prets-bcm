package mr.bcm.gestionprets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmployeRequest(
        @NotBlank String matricule,
        @NotBlank String nom,
        String departement,
        @Email String email) {
}
