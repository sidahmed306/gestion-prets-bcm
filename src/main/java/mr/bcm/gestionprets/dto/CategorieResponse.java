package mr.bcm.gestionprets.dto;

import mr.bcm.gestionprets.entity.Categorie;

public record CategorieResponse(Long id, String code, String libelle) {

    public static CategorieResponse of(Categorie categorie) {
        return new CategorieResponse(categorie.getId(), categorie.getCode(), categorie.getLibelle());
    }
}
