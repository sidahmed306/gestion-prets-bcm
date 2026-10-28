package mr.bcm.gestionprets.annuaire;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import mr.bcm.gestionprets.entity.Role;

/**
 * Toutes les options "app.annuaire.*" de application.properties, réunies dans un seul objet.
 * Spring remplit automatiquement chaque champ : "base-utilisateurs" → baseUtilisateurs, etc.
 */
@ConfigurationProperties(prefix = "app.annuaire")
public record AnnuaireProperties(
        boolean enabled,
        String baseUtilisateurs,
        String filtreConnexion,
        String attributMatricule,
        String attributDepartement,
        String attributPoste,
        List<RegleAcces> regles) {

    public AnnuaireProperties {
        regles = regles != null ? regles : List.of();
    }

    /**
     * Une règle d'accès : « si l'agent est dans telle direction ET a tel poste, alors tel rôle ».
     *
     * @param departement nom exact de la direction ; null = n'importe quelle direction
     * @param postes      le poste doit commencer par l'un de ces mots ; vide = n'importe quel poste
     */
    public record RegleAcces(Role role, String departement, List<String> postes) {

        public boolean correspond(String departementAgent, String posteAgent) {
            return departementCorrespond(departementAgent) && posteCorrespond(posteAgent);
        }

        private boolean departementCorrespond(String departementAgent) {
            return departement == null || departement.equalsIgnoreCase(nettoyer(departementAgent));
        }

        private boolean posteCorrespond(String posteAgent) {
            if (postes == null || postes.isEmpty()) {
                return true;
            }
            String poste = nettoyer(posteAgent).toLowerCase();
            return postes.stream().anyMatch(debut -> poste.startsWith(debut.trim().toLowerCase()));
        }

        private static String nettoyer(String valeur) {
            return valeur == null ? "" : valeur.trim();
        }
    }
}
