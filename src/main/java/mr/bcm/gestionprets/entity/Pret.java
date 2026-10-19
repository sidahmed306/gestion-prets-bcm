package mr.bcm.gestionprets.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "prets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Pret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "materiel_id", nullable = false)
    @ToString.Exclude
    private Materiel materiel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    @ToString.Exclude
    private Employe employe;

    @NotNull
    @Column(nullable = false)
    private LocalDate datePret;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateRetourPrevue;

    private LocalDate dateRetourEffective;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPret statut;

    @Column(length = 1000)
    private String commentaire;

    @AssertTrue(message = "La date de retour prévue doit être postérieure ou égale à la date de prêt")
    @Transient
    public boolean isDateRetourPrevueValide() {
        return datePret == null || dateRetourPrevue == null || !dateRetourPrevue.isBefore(datePret);
    }

    @Transient
    public boolean isEnRetard() {
        return statut == StatutPret.EN_COURS
                && dateRetourPrevue != null
                && LocalDate.now().isAfter(dateRetourPrevue);
    }
}
