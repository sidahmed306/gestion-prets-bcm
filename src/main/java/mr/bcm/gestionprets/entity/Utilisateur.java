package mr.bcm.gestionprets.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compte qui peut se connecter à l'application (à ne pas confondre avec Employe,
 * qui est la personne qui emprunte du matériel).
 */
@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    /** Mot de passe haché avec BCrypt : jamais stocké en clair. */
    @NotBlank
    @Column(nullable = false)
    private String motDePasse;

    @NotBlank
    @Column(nullable = false)
    private String nomComplet;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Un compte désactivé ne peut plus se connecter, et ses jetons existants sont refusés. */
    @Column(nullable = false)
    private boolean actif;

    private LocalDateTime derniereConnexion;
}
