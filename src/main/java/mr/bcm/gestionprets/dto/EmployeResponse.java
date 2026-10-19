package mr.bcm.gestionprets.dto;

import mr.bcm.gestionprets.entity.Employe;

public record EmployeResponse(Long id, String matricule, String nom, String departement, String email) {

    public static EmployeResponse of(Employe employe) {
        return new EmployeResponse(
                employe.getId(),
                employe.getMatricule(),
                employe.getNom(),
                employe.getDepartement(),
                employe.getEmail());
    }
}
