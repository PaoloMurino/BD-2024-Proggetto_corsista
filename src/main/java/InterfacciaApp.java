import java.sql.SQLException;
import java.util.Scanner;

public class InterfacciaApp {
    private Database database;
    private Query query;
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        InterfacciaApp interfacciaApp = new InterfacciaApp();

        interfacciaApp.connessione();
        interfacciaApp.interfaccia();
        interfacciaApp.database.close();

        System.out.println("-------------- Connessione terminata --------------");
    }

    private void connessione() throws SQLException, ClassNotFoundException {
        System.out.println("-------------- Connessione al database --------------");

        /*System.out.print("Inserire il nome del database: ");
        String databaseName = scanner.nextLine();
        System.out.print("Inserire nome utente: ");
        String username = scanner.nextLine();
        System.out.print("Inserire password: ");
        String password = scanner.nextLine();*/

        //database = new Database(databaseName, username, password);
        database = new Database("servizidiformazione", "gestoreCorsi", "basididati2024");
        query = new Query(database);

        database.connect();
    }

    private void interfaccia() {
        int sel;

        while (true) {
            System.out.println("\n-------------- Interfaccia query --------------");
            System.out.println("""
                    1. Registrare un corso a catalogo
                    2. Iscrivere un'azienda ad una classe
                    3. Richiesta di un corso personalizzato
                    4. Aggiunta di un nuovo docente ad una classe
                    5. Modifica del docente a cui è affidato un corso personalizzato
                    6. Stampa di tutti i corsi a catalogo messi a disposizione da un'azienda erogatrice
                    7. Stampa di tutte le aziende erogatrici non impegnate in corsi personalizzati
                    8. Verifica della possibilità di assegnare un docente ad un corso
                    9. Verifica l’eventuale presenza di docenti attualmente non coinvolti in corsi
                    10. Per ciascun corso a catalogo, stampare il numero totale di discenti
                    11. Stampa i dati del docente maggiormente impiegato in corsi (a catalogo e/o personalizzato)
                    12. Stampa di tutti i corsi a catalogo per i quali non si è mai formata più di una classe
                    13. Stampa dei dati delle aziende erogatrici, compreso il ricavo totale che hanno ottenuto dall’erogazione di tutte le tipologie di corsi
                    14. Stampa di ogni classe, compreso il ricavo ottenuto mediante la definizione della stessa
                    15. Stampa una classifica delle aziende fruitrici sulla base del numero di servizi che ha richiesto
                    0. Esci
                    """);

            System.out.print("Selezione: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            sel = scanner.nextInt();

            System.out.println();

            switch (sel) {
                case 1 -> query.query1();
                case 2 -> query.query2();
                case 3 -> query.query3();
                case 4 -> query.query4();
                case 5 -> query.query5();
                case 6 -> query.query6();
                case 7 -> query.query7();
                case 8 -> query.query8();
                case 9 -> query.query9();
                case 10 -> query.query10();
                case 11 -> query.query11();
                case 12 -> query.query12();
                case 13 -> query.query13();
                case 14 -> query.query14();
                case 15 -> query.query15();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Nessuna query è associata al numero " + sel);
            }

            // Ritardo prima di far ripartire il ciclo per maggiore leggibilità
            try {
                Thread.sleep(2100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione del thread
                System.out.println("Il ritardo è stato interrotto.");
            }
        }
    }
}