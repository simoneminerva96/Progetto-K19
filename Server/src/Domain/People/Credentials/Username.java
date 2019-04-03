// CRIS

package Domain.People.Credentials;

// classe responsabile della creazione, controllo conformità e stampa dello username
public class Username {
    private CharAnalizer username;

    public Username(String  username) {
        this.username = new CharAnalizer(username);
        checkValidity();
    }

    // checkValidity() garantisce che lo username sia di almeno 1 e fino a 16 caratteri, non accettando caratteri speciali o spazi
    private CharAnalizer checkValidity() {
        if (username.getLine().length() > 0) {
            if (username.getLine().length() <= 16) {
                if (!username.isSpecialChar()) {
                    /*if (controlloDoppioni()) {
                        System.out.println("USERNAME CREATO");
                        return username;
                    } else {
                        System.out.println("USERNAME INVALIDO\nUSERNAME SCELTO NON DISPONIBILE");
                        return username = null;
                    }*/
                    System.out.println("USERNAME SUCCESSFULLY CREATED");
                    return username;
                } else {
                    System.out.println("USERNAME REJECTED\nONLY LETTERS AND NUMBERS ALLOWED");
                    return username = null;
                }
            } else {
                System.out.println("USERNAME REJECTED\nMAXIMUM DIMENSION 16 CHARACTERS");
                return username = null;
            }
        } else {
            System.out.println("USERNAME REJECTED\nRETYPE USERNAME");
            return username = null;
        }
    }

    // getUsername() stampa l'oggetto username come una stringa
    public String getUsername() {
        return username.getLine();
    }
}
