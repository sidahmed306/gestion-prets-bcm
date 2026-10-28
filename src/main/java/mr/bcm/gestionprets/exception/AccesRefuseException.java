package mr.bcm.gestionprets.exception;

/** La personne est bien identifiée, mais n'a pas le droit d'utiliser l'application (→ 403). */
public class AccesRefuseException extends RuntimeException {

    public AccesRefuseException(String message) {
        super(message);
    }
}
