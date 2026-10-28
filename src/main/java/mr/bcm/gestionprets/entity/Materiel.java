package mr.bcm.gestionprets.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "materiels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Materiel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String numeroInventaire;

    private String designation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    @ToString.Exclude
    private Categorie categorie;

    private String marque;

    private String modele;

    private String etatPhysique;

    // --- Informations comptables (facultatives) ---

    /** Prix d'achat en ouguiyas (MRU), 2 décimales. BigDecimal et non double : pas d'erreur d'arrondi sur l'argent. */
    @PositiveOrZero
    @Column(precision = 15, scale = 2)
    private BigDecimal prixAchat;

    @PastOrPresent
    private LocalDate dateAchat;

    /** Durée sur laquelle le matériel perd sa valeur (ex : 3 ans pour un ordinateur). */
    @Min(1)
    @Max(50)
    private Integer dureeAmortissementAnnees;

    private String fournisseur;

    /** Le calcul d'amortissement n'est possible que si les 3 informations sont renseignées. */
    @Transient
    public boolean isValorise() {
        return prixAchat != null && dateAchat != null && dureeAmortissementAnnees != null;
    }
}
