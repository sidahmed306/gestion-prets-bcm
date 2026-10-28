package mr.bcm.gestionprets.comptabilite;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mr.bcm.gestionprets.dto.LigneAmortissementResponse;
import mr.bcm.gestionprets.dto.PlanAmortissementResponse;
import mr.bcm.gestionprets.dto.SituationComptableResponse;
import mr.bcm.gestionprets.dto.SyntheseComptableResponse;
import mr.bcm.gestionprets.entity.Materiel;
import mr.bcm.gestionprets.exception.BusinessRuleException;
import mr.bcm.gestionprets.repository.MaterielRepository;
import mr.bcm.gestionprets.service.MaterielService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComptabiliteService {

    private final MaterielRepository materielRepository;
    private final MaterielService materielService;

    /** Additionne la valeur de tous les matériels valorisés, au total et par catégorie. */
    public SyntheseComptableResponse synthese(LocalDate date) {
        List<Materiel> materiels = materielRepository.findAll();
        BigDecimal valeurAchat = BigDecimal.ZERO;
        BigDecimal cumul = BigDecimal.ZERO;
        BigDecimal dotationAnnee = BigDecimal.ZERO;
        int valorises = 0;
        int totalementAmortis = 0;
        Map<String, BigDecimal[]> parCategorie = new TreeMap<>(); // [nombre, valeur d'achat, valeur nette]

        for (Materiel materiel : materiels) {
            if (!materiel.isValorise()) {
                continue;
            }
            SituationComptableResponse situation = CalculAmortissement.situation(materiel.getPrixAchat(),
                    materiel.getDateAchat(), materiel.getDureeAmortissementAnnees(), date);
            valorises++;
            if (situation.totalementAmorti()) {
                totalementAmortis++;
            }
            valeurAchat = valeurAchat.add(materiel.getPrixAchat());
            cumul = cumul.add(situation.amortissementCumule());
            dotationAnnee = dotationAnnee.add(dotationDeLAnnee(materiel, date.getYear()));

            BigDecimal[] totaux = parCategorie.computeIfAbsent(materiel.getCategorie().getLibelle(),
                    libelle -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO });
            totaux[0] = totaux[0].add(BigDecimal.ONE);
            totaux[1] = totaux[1].add(materiel.getPrixAchat());
            totaux[2] = totaux[2].add(situation.valeurNetteComptable());
        }

        List<SyntheseComptableResponse.ParCategorie> categories = new ArrayList<>();
        parCategorie.forEach((libelle, totaux) -> categories.add(
                new SyntheseComptableResponse.ParCategorie(libelle, totaux[0].intValue(), totaux[1], totaux[2])));

        return new SyntheseComptableResponse(date, materiels.size(), valorises, materiels.size() - valorises,
                totalementAmortis, valeurAchat, cumul, valeurAchat.subtract(cumul), dotationAnnee, categories);
    }

    public PlanAmortissementResponse planAmortissement(Long materielId) {
        Materiel materiel = materielService.findById(materielId);
        if (!materiel.isValorise()) {
            throw new BusinessRuleException("Le matériel " + materiel.getNumeroInventaire()
                    + " n'a pas de prix, de date d'achat ou de durée d'amortissement");
        }
        return new PlanAmortissementResponse(materiel.getId(), materiel.getNumeroInventaire(),
                materiel.getDesignation(), materiel.getPrixAchat(), materiel.getDateAchat(),
                materiel.getDureeAmortissementAnnees(),
                CalculAmortissement.annuite(materiel.getPrixAchat(), materiel.getDureeAmortissementAnnees()),
                CalculAmortissement.plan(materiel.getPrixAchat(), materiel.getDateAchat(),
                        materiel.getDureeAmortissementAnnees()));
    }

    /** La dotation de l'exercice demandé, telle qu'elle figure dans le plan (0 si hors plan). */
    private BigDecimal dotationDeLAnnee(Materiel materiel, int annee) {
        return CalculAmortissement.plan(materiel.getPrixAchat(), materiel.getDateAchat(),
                        materiel.getDureeAmortissementAnnees())
                .stream()
                .filter(ligne -> ligne.exercice() == annee)
                .map(LigneAmortissementResponse::dotation)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
