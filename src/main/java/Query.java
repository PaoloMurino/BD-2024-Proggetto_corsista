import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class Query {
    private final Database database;
    private final Scanner scanner = new Scanner(System.in);

    public Query(Database database) {
        this.database = database;
    }

    // 1. Registrazione di un corso a catalogo
    public void query1() {
        String query = "INSERT INTO CorsoCatalogo (Titolo, Descrizione, NumOre, Modalita, Settore, Argomenti, " +
                "TipoServizio, CostoAPersona) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)) {
            System.out.println("\n-------------- Registrazione corso a catalogo! --------------");
            System.out.print("Inserisci Titolo: ");
            String titolo = scanner.nextLine();
            preparedStatement.setString(1, titolo);

            System.out.print("Inserisci Descrizione: ");
            String descrizione = scanner.nextLine();
            preparedStatement.setString(2, descrizione);

            System.out.print("Inserisci numero ore: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int numOre = scanner.nextInt();
            preparedStatement.setInt(3, numOre);
            scanner.nextLine(); // Pulizia del buffer

            System.out.print("Inserisci modalità (Presenza, Distanza, Ibrida): ");
            String modalita = scanner.nextLine();
            while (!modalita.matches("Presenza|Distanza|Ibrida")) {
                System.out.println("Errore: Inserisci una modalità valida: Presenza, Distanza, Ibrida!");
                modalita = scanner.nextLine();
            }
            preparedStatement.setString(4, modalita);

            System.out.print("Inserisci settore: ");
            String settore = scanner.nextLine();
            preparedStatement.setString(5, settore);

            System.out.print("Inserisci argomenti: ");
            String argomenti = scanner.nextLine();
            preparedStatement.setString(6, argomenti);

            System.out.print("Inserisci la tipologia del servizio (Lezioni, Laboratorio, Seminari): ");
            String tipoServizio = scanner.nextLine();
            while (!tipoServizio.matches("Lezioni|Laboratorio|Seminari")) {
                System.out.println("Errore: Inserisci una tipologia valida: Lezioni, Laboratorio, Seminari!");
                tipoServizio = scanner.nextLine();
            }
            preparedStatement.setString(7, tipoServizio);

            System.out.print("Inserisci il costo a persona: ");
            while (!scanner.hasNextDouble()) {
                System.out.println("Errore: Inserisci un valore numerico valido per il costo!");
                scanner.next();
            }
            double costoAPersona = scanner.nextDouble();
            preparedStatement.setDouble(8, costoAPersona);
            scanner.nextLine(); // Pulizia del buffer

            // Esecuzione della query
            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Corso a catalogo inserito con successo! --------------");
            } else {
                System.out.println("\n-------------- Errore durante l'inserimento del corso. --------------");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 2. Iscrizione di un'azienda a una classe
    public void query2() {
        String query = "INSERT INTO Iscrizione (Azienda, DataInizioClasse, DataFineClasse, CorsoClasse, " +
                "NumDipendenti) VALUES (?, ?, ?, ?, ?)";
        String trovaClasse = """
                    SELECT Classe.DataScadenzaIscrizioni
                    FROM Classe
                    WHERE Classe.CorsoCatalogo = ?
                    AND Classe.DataInizio = ?
                    AND Classe.DataFine = ?
                    """;
        String trovaAzienda = """
                SELECT Azienda.Ruolo, Azienda.NumDipendenti
                FROM Azienda
                WHERE Azienda.PIVA = ?
                """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             PreparedStatement checkClasseStatement = database.getConnection().prepareStatement(trovaClasse);
             PreparedStatement checkAziendaStatement = database.getConnection().prepareStatement(trovaAzienda)) {
            System.out.println("\n-------------- Iscrizione di un'Azienda ad una classe! --------------");
            System.out.print("Inserisci la P.IVA dell'Azienda da iscrivere: ");
            String azienda = scanner.nextLine();
            preparedStatement.setString(1, azienda);
            checkAziendaStatement.setString(1, azienda);

            int aziendaDipendenti;  // Variabile per il controllo del numero di dipendenti
            // controllo sul ruolo dell'azienda
            try (ResultSet resultSet = checkAziendaStatement.executeQuery()) {
                if (resultSet.next()) {
                    String ruolo = resultSet.getString("Ruolo");
                    aziendaDipendenti = resultSet.getInt("NumDipendenti");

                    if (!"Fruitrice".equalsIgnoreCase(ruolo) && !"Entrambe".equalsIgnoreCase(ruolo)) {
                        System.out.println("Errore: Non è possibile iscrivere un'azienda erogatrice!");
                        return;
                    }
                } else {
                    System.out.println("Errore: L'azienda specificata non esiste.");
                    return;
                }
            }

            System.out.print("Inserisci il corso erogato dalla classe: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int corsoClasse = scanner.nextInt();
            preparedStatement.setInt(4, corsoClasse);
            checkClasseStatement.setInt(1, corsoClasse);
            scanner.nextLine(); // Pulizia del buffer

            System.out.print("Inserisci la data di inizio della classe (formato YYYY-MM-DD): ");
            Date dataInizio = null;
            while (dataInizio == null) {
                String dataInizioInput = scanner.nextLine();
                try {
                    dataInizio = Date.valueOf(dataInizioInput);
                } catch (IllegalArgumentException e) {
                    System.out.println("Errore: Formato data non valido! Riprova (esempio: 2024-12-30): ");
                }
            }
            preparedStatement.setDate(2, dataInizio);
            checkClasseStatement.setDate(2, dataInizio);

            System.out.print("Inserisci la data di fine della classe (formato YYYY-MM-DD): ");
            Date dataFine = null;
            while (dataFine == null) {
                String dataFineInput = scanner.nextLine();
                try {
                    dataFine = Date.valueOf(dataFineInput);
                } catch (IllegalArgumentException e) {
                    System.out.println("Errore: Formato data non valido! Riprova (esempio: 2024-12-30): ");
                }
            }
            preparedStatement.setDate(3, dataFine);
            checkClasseStatement.setDate(3, dataFine);

            // controllo sulla data di scadenza iscrizioni
            try (ResultSet resultSet = checkClasseStatement.executeQuery()) {
                if (resultSet.next()) {
                    Date dataScadenzaIscrizioni = resultSet.getDate("DataScadenzaIscrizioni");
                    Date dataOggi = new Date(System.currentTimeMillis());

                    if (dataOggi.after(dataScadenzaIscrizioni)) {
                        System.out.println("Errore: Non è possibile iscrivere l'azienda. La data di scadenza iscrizioni è già passata!");
                        return;
                    }
                } else {
                    System.out.println("Errore: La classe specificata non esiste.");
                    return;
                }
            }

            System.out.print("Inserisci il numero di dipendenti da iscrivere: ");
            int numDipendenti = -1; // Valore non valido per l'inserimento
            do {
                while (!scanner.hasNextInt()) {
                    System.out.println("Errore: Inserisci un numero valido!");
                    scanner.next(); // Pulizia del buffer per input non valido
                }
                numDipendenti = scanner.nextInt();
                if (numDipendenti > aziendaDipendenti) {
                    System.out.println("Errore: L'azienda specificata non ha tutti questi dipendenti! Riprova.");
                } else {
                    break; // Esce dal ciclo se il valore è valido
                }
            } while (true);
            preparedStatement.setInt(5, numDipendenti);
            scanner.nextLine(); // Pulizia del buffer

            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Azienda iscritta con successo! --------------");
            } else {
                System.out.println("\n-------------- Errore durante l'iscrizione dell'Azienda. --------------");
            }

            // Aggiornamento attributo RicavoClasse
            String updateQuery = """
                UPDATE Classe
                SET RicavoClasse = RicavoClasse + (
                    SELECT ? * costoAPersona
                    FROM CorsoCatalogo
                    WHERE CorsoCatalogo.Id = ?
                )
                WHERE Classe.CorsoCatalogo = ?
                AND Classe.DataInizio = ?
                AND Classe.DataFine = ?
            """;
            try (PreparedStatement updateStatement = database.getConnection().prepareStatement(updateQuery)) {
                updateStatement.setInt(1, numDipendenti); // Parametro per il numero di dipendenti
                updateStatement.setInt(2, corsoClasse); // Parametro per identificare il corso a catalogo
                updateStatement.setInt(3, corsoClasse); // Parametro per identificare la classe
                updateStatement.setDate(4, dataInizio); // Parametro per identificare la classe
                updateStatement.setDate(5, dataFine); // Parametro per identificare la classe
                int tupleAggiornate = updateStatement.executeUpdate();
                if (tupleAggiornate > 0) {
                    System.out.println("Ricavo Classe aggiornato con successo!");
                } else {
                    System.out.println("Errore durante l'aggiornamento di Ricavo Classe.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 3. Richiesta Corso personalizzato
    public void query3() {
        String query = "INSERT INTO CorsoPersonalizzato (Titolo, Descrizione, NumOre, Modalita, TipoServizio, DataInizio, " +
                "DataFine, TestDMA, NumeroTrasferte, CostoTotale) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean richiestaCreata = false;    // per assicurarsi che la richiesta sia stata creata
        Integer idCorsoPersonalizzato = null;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            System.out.println("\n-------------- Richiesta Corso personalizzato! --------------");
            System.out.print("Inserisci Titolo: ");
            String titolo = scanner.nextLine();
            preparedStatement.setString(1, titolo);

            System.out.print("Inserisci Descrizione: ");
            String descrizione = scanner.nextLine();
            preparedStatement.setString(2, descrizione);

            System.out.print("Inserisci numero ore: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int numOre = scanner.nextInt();
            preparedStatement.setInt(3, numOre);
            scanner.nextLine(); // Pulizia del buffer

            System.out.print("Inserisci modalità (Presenza, Distanza, Ibrida): ");
            String modalita = scanner.nextLine();
            while (!modalita.matches("Presenza|Distanza|Ibrida")) {
                System.out.println("Errore: Inserisci una modalità valida: Presenza, Distanza, Ibrida!");
                modalita = scanner.nextLine();
            }
            preparedStatement.setString(4, modalita);

            System.out.print("Inserisci la tipologia del servizio (Lezioni, Laboratorio, Seminari): ");
            String tipoServizio = scanner.nextLine();
            while (!tipoServizio.matches("Lezioni|Laboratorio|Seminari")) {
                System.out.println("Errore: Inserisci una tipologia valida: Lezioni, Laboratorio, Seminari!");
                tipoServizio = scanner.nextLine();
            }
            preparedStatement.setString(5, tipoServizio);

            System.out.print("Inserisci la data di inizio del corso (formato YYYY-MM-DD): ");
            Date dataInizio = null;
            while (dataInizio == null) {
                String dataInizioInput = scanner.nextLine();
                try {
                    // Converte l'input in LocalDate
                    LocalDate dataInserita = LocalDate.parse(dataInizioInput);

                    // Verifica che la data non sia nel passato
                    if (dataInserita.isBefore(LocalDate.now())) {
                        System.out.println("Errore: La data di inizio del corso non può essere nel passato! Riprova: ");
                    } else {
                        // Se la data è valida, convertila in java.sql.Date
                        dataInizio = Date.valueOf(dataInserita);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Errore: Formato data non valido! Riprova (esempio: 2024-12-30): ");
                }
            }
            preparedStatement.setDate(6, dataInizio);

            System.out.print("Inserisci la data di fine del corso (formato YYYY-MM-DD): ");
            Date dataFine = null;
            while (dataFine == null) {
                String dataFineInput = scanner.nextLine();
                try {
                    dataFine = Date.valueOf(dataFineInput);
                } catch (IllegalArgumentException e) {
                    System.out.println("Errore: Formato data non valido! Riprova (esempio: 2024-12-30): ");
                }
            }
            preparedStatement.setDate(7, dataFine);

            System.out.print("Il corso richiede il TestDMA? (Sì/No): ");
            String testDMAInput = scanner.nextLine();
            while (!testDMAInput.equalsIgnoreCase("Sì") && !testDMAInput.equalsIgnoreCase("No")) {
                System.out.println("Errore: Rispondi con 'Sì' o 'No'.");
                testDMAInput = scanner.nextLine();
            }
            boolean testDMA = testDMAInput.equalsIgnoreCase("Sì");
            preparedStatement.setBoolean(8, testDMA);


            System.out.print("Inserisci il numero di trasferte da effettuare: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int numTrasferte = scanner.nextInt();
            preparedStatement.setInt(9, numTrasferte);
            scanner.nextLine(); // Pulizia del buffer

            System.out.print("Inserisci il costo totale del corso: ");
            while (!scanner.hasNextDouble()) {
                System.out.println("Errore: Inserisci un valore numerico valido per il costo!");
                scanner.next();
            }
            double costoTotale = scanner.nextDouble();
            preparedStatement.setDouble(10, costoTotale);
            scanner.nextLine(); // Pulizia del buffer

            // Esecuzione della query
            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Corso personalizzato creato con successo! --------------");

                // Ottenere l'ID del corso appena creato
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idCorsoPersonalizzato = generatedKeys.getInt(1); // Recupera l'ID del corso

                        // Creazione della richiesta
                        String queryRichiesta = "INSERT INTO Richiesta (Azienda, DataRichiesta, CorsoPersonalizzato) VALUES (?, ?, ?)";
                        String trovaAzienda = """
                            SELECT Azienda.Ruolo
                            FROM Azienda
                            WHERE Azienda.PIVA = ?
                        """;

                        try (PreparedStatement richiestaStatement = database.getConnection().prepareStatement(queryRichiesta);
                             PreparedStatement checkAziendaStatement = database.getConnection().prepareStatement(trovaAzienda)) {
                            System.out.print("Inserisci la P.IVA dell'Azienda che ha effettuato la richiesta: ");
                            String azienda = scanner.nextLine();
                            richiestaStatement.setString(1, azienda);
                            checkAziendaStatement.setString(1, azienda);

                            // Controllo sul ruolo dell'azienda
                            try (ResultSet resultSet = checkAziendaStatement.executeQuery()) {
                                if (resultSet.next()) {
                                    String ruolo = resultSet.getString("Ruolo");

                                    if (!"Fruitrice".equalsIgnoreCase(ruolo) && !"Entrambe".equalsIgnoreCase(ruolo)) {
                                        System.out.println("Errore: Non è possibile registrare una richiesta di un'azienda erogatrice!");
                                        eliminaCorsoP(idCorsoPersonalizzato);
                                        return;
                                    }
                                } else {
                                    System.out.println("Errore: L'azienda specificata non esiste.");
                                    eliminaCorsoP(idCorsoPersonalizzato);
                                    return;
                                }
                            }

                            // Imposta la data odierna
                            Date dataRichiesta = Date.valueOf(LocalDate.now());
                            richiestaStatement.setDate(2, dataRichiesta);

                            // Imposta l'ID del corso personalizzato
                            richiestaStatement.setInt(3, idCorsoPersonalizzato);

                            // Esecuzione della query per creare la richiesta
                            int tupleInseriteRichiesta = richiestaStatement.executeUpdate();
                            if (tupleInseriteRichiesta > 0) {
                                richiestaCreata = true;
                                System.out.println("\n-------------- Richiesta creata con successo! --------------");
                            } else {
                                System.out.println("\n-------------- Errore durante la creazione della richiesta! --------------");
                            }
                        }

                        creaGestioneCorso(idCorsoPersonalizzato);
                    } else {
                        System.out.println("Errore: Impossibile ottenere l'ID del corso personalizzato appena creato!");
                    }
                }
            } else {
                System.out.println("\n-------------- Errore durante la creazione del corso. --------------");
            }
        } catch (SQLException e) {
            handleSQLException(e);
            if(!richiestaCreata && idCorsoPersonalizzato != null)
                eliminaCorsoP(idCorsoPersonalizzato);
        }
    }

    private void creaGestioneCorso(int idCorsoPersonalizzato) {
        String queryGestione = "INSERT INTO Gestione (Docente, CorsoPersonalizzato) VALUES (?, ?)";
        String queryVerificaDocente = "SELECT COUNT(*) FROM Docente WHERE CF = ?"; // Controlla se il docente esiste nel database
        boolean gestioneCreato = false;

        while (!gestioneCreato) {
            try (PreparedStatement verificaStatement = database.getConnection().prepareStatement(queryVerificaDocente)) {
                System.out.print("Inserisci il CF del docente che gestirà il corso: ");
                String docente = scanner.nextLine();

                // Verifica se il docente esiste nel database
                verificaStatement.setString(1, docente);
                try (ResultSet resultSet = verificaStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        // Il docente esiste
                        try (PreparedStatement gestioneStatement = database.getConnection().prepareStatement(queryGestione)) {
                            gestioneStatement.setString(1, docente);
                            gestioneStatement.setInt(2, idCorsoPersonalizzato);

                            // Esecuzione della query per creare la gestione
                            int tupleInseriteGestione = gestioneStatement.executeUpdate();
                            if (tupleInseriteGestione > 0) {
                                System.out.println("\n-------------- Gestione creata con successo! --------------");
                                gestioneCreato = true;
                            } else {
                                System.out.println("\n-------------- Errore durante la creazione della gestione! --------------");
                                System.out.println("Per favore, inserisci un nuovo docente.");
                            }
                        }
                    } else {
                        System.out.println("Errore: Il CF inserito non corrisponde a nessun docente nel database.");
                        System.out.println("Per favore, inserisci un codice fiscale valido.");
                    }
                }
            } catch (SQLException e) {
                // Gestione dell'errore del trigger RV1
                if (e.getSQLState().equals("45000")) {
                    System.out.println("Errore: Il docente è già assegnato a 3 corsi personalizzati.");
                    System.out.println("Prova a inserire un altro docente.");
                } else {
                    handleSQLException(e);
                    gestioneCreato = true; // Esce dal ciclo in caso di errore irreversibile
                }
            }
        }
    }

    // 4. Aggiunta di un nuovo docente a una classe
    public void query4() {
        String query = "INSERT INTO Collaborazione (Docente, CorsoClasse, DataInizioClasse, DataFineClasse)  " +
                "VALUES (?, ?, ?, ?)";
        String classeQuery = """
            SELECT Azienda
            FROM Classe
            WHERE CorsoCatalogo = ?
            AND DataInizio = ?
            AND DataFine = ?
            """;

        String docenteQuery = """
            SELECT Docente
            FROM Collaborazione
            WHERE CorsoClasse = ?
            AND DataInizioClasse = ?
            AND DataFineClasse = ?
            """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             PreparedStatement checkClasseStatement = database.getConnection().prepareStatement(classeQuery);
             PreparedStatement checkDocenteStatement = database.getConnection().prepareStatement(docenteQuery)) {

            System.out.println("\n-------------- Aggiunta di un nuovo docente ad una classe! --------------");

            System.out.print("Inserisci il corso erogato dalla classe: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int corsoClasse = scanner.nextInt();
            preparedStatement.setInt(2, corsoClasse);
            checkClasseStatement.setInt(1, corsoClasse);
            checkDocenteStatement.setInt(1, corsoClasse);
            scanner.nextLine(); // Pulizia del buffer

            System.out.print("Inserisci la data di inizio della classe (formato YYYY-MM-DD): ");
            Date dataInizio = null;
            while (dataInizio == null) {
                String dataInizioInput = scanner.nextLine();
                try {
                    dataInizio = Date.valueOf(dataInizioInput);
                } catch (IllegalArgumentException e) {
                    System.out.println("Errore: Formato data non valido! Riprova (esempio: 2024-12-30): ");
                }
            }
            preparedStatement.setDate(3, dataInizio);
            checkClasseStatement.setDate(2, dataInizio);
            checkDocenteStatement.setDate(2, dataInizio);

            System.out.print("Inserisci la data di fine della classe (formato YYYY-MM-DD): ");
            Date dataFine = null;
            while (dataFine == null) {
                String dataFineInput = scanner.nextLine();
                try {
                    dataFine = Date.valueOf(dataFineInput);
                    LocalDate oggi = LocalDate.now();
                    if (dataFine.toLocalDate().isBefore(oggi)) {
                        System.out.println("Errore: Non puoi aggiungere un docente ad una classe che è terminata!");
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Errore: Formato data non valido! Riprova (esempio: 2024-12-30): ");
                }
            }
            preparedStatement.setDate(4, dataFine);
            checkClasseStatement.setDate(3, dataFine);
            checkDocenteStatement.setDate(3, dataFine);

            // Trova la Classe
            ResultSet resultSetClasse = checkClasseStatement.executeQuery();
            String aziendaClasse = null;
            if (resultSetClasse.next()) {
                aziendaClasse = resultSetClasse.getString("Azienda");
            }

            // Trova il docente attualmente associato alla classe
            ResultSet resultSetDocente = checkDocenteStatement.executeQuery();
            String docenteAttuale = null;
            if (resultSetDocente.next()) {
                docenteAttuale = resultSetDocente.getString("Docente");
            }

            if (docenteAttuale != null) {
                boolean docentiDisponibili = stampaDocentiAzienda(aziendaClasse, docenteAttuale);
                if (!docentiDisponibili){
                    return;
                }
            }

            System.out.print("Inserisci il CF del docente: ");
            String docente = scanner.nextLine();
            preparedStatement.setString(1, docente);

            // Esecuzione della query
            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Docente aggiunto con successo! --------------");
            } else {
                System.out.println("\n-------------- Errore durante l'aggiunta del docente. --------------");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }


    // 5. Modifica del docente a cui è affidato un corso personalizzato
    public void query5() {
        String updateQuery = """
            UPDATE Gestione
            SET Docente = ?
            WHERE Gestione.CorsoPersonalizzato = ?
        """;
        String query = """
            SELECT Azienda.PIVA, Gestione.Docente
            FROM Azienda
            JOIN Dipendente ON Azienda.PIVA = Dipendente.Azienda
            JOIN Docente ON Dipendente.CF = Docente.CF
            JOIN Gestione ON Docente.CF = Gestione.Docente
            JOIN CorsoPersonalizzato ON Gestione.CorsoPersonalizzato = CorsoPersonalizzato.ID
            WHERE CorsoPersonalizzato.ID = ?
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(updateQuery);
             PreparedStatement aziendaStatement = database.getConnection().prepareStatement(query)) {

            System.out.println("\n-------------- Modifica docente di un Corso personalizzato! --------------");

            System.out.print("Inserisci il corso di cui si vuole modificare il docente: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int corsoPersonalizzato = scanner.nextInt();
            preparedStatement.setInt(2, corsoPersonalizzato);
            aziendaStatement.setInt(1, corsoPersonalizzato);
            scanner.nextLine(); // Pulizia del buffer

            // Ottiene l'azienda e il docente attuale del corso personalizzato
            ResultSet resultSet = aziendaStatement.executeQuery();
            if (resultSet.next()) {
                String azienda = resultSet.getString("PIVA");
                String docenteAttuale = resultSet.getString("Docente");

                // Stampa i docenti che lavorano per la stessa azienda, escludendo quello attuale
                boolean docentiDisponibili = stampaDocentiAzienda(azienda, docenteAttuale);
                if (!docentiDisponibili) {
                    return;
                }
            }

            System.out.print("Inserisci il nuovo docente: ");
            String docente = scanner.nextLine();
            preparedStatement.setString(1, docente);

            // Esegui la query per aggiornare il docente
            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Docente modificato con successo! --------------");
            } else {
                System.out.println("\n-------------- Errore durante la modifica del docente. --------------");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }


    private boolean stampaDocentiAzienda(String Azienda, String docenteAttuale) {
        String query = """
        SELECT Docente.CF
        FROM Azienda
        JOIN Dipendente ON Azienda.PIVA = Dipendente.Azienda
        JOIN Docente ON Dipendente.CF = Docente.CF
        WHERE Azienda.PIVA = ? AND Docente.CF != ?
        """;

        try(PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)){
            preparedStatement.setString(1, Azienda);
            preparedStatement.setString(2, docenteAttuale);

            ResultSet resultSet = preparedStatement.executeQuery();

            boolean docentiTrovati = false; // Flag per verificare se ci sono altri docenti
            System.out.println("\nDocenti che lavorano per l'azienda selezionata:");
            while(resultSet.next()) {
                String docente = resultSet.getString(1);
                System.out.println("- " + docente);
                docentiTrovati = true;
            }

            if (!docentiTrovati) {
                System.out.println("\nNessun altro docente disponibile per l'azienda selezionata.");
                return false;
            }

        } catch(SQLException e){
            handleSQLException(e);
        }
        return true;
    }



    // 6. Stampa di tutti i corsi a catalogo messi a disposizione da un'azienda erogatrice
    public void query6() {
        String query = """
            SELECT DISTINCT CorsoCatalogo.*
            FROM Classe
            JOIN CorsoCatalogo ON Classe.CorsoCatalogo = CorsoCatalogo.ID
            WHERE Classe.Azienda = ?
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)) {
            System.out.println("\n-------------- Stampa di tutti i corsi a catalogo erogati da un'Azienda erogatrice! --------------");
            System.out.print("Inserisci la P.IVA dell'Azienda: ");
            String azienda = scanner.nextLine();
            preparedStatement.setString(1, azienda);

            // Variabile per il nome dell'azienda
            String nomeAzienda;

            // Query per ottenere il nome dell'azienda
            String queryAzienda = """
                SELECT Nome
                FROM Azienda
                WHERE Azienda.PIVA = ?
            """;
            try (PreparedStatement aziendaStatement = database.getConnection().prepareStatement(queryAzienda)) {
                aziendaStatement.setString(1, azienda);

                try (ResultSet resultAzienda = aziendaStatement.executeQuery()) {
                    if (resultAzienda.next()) {
                        nomeAzienda = resultAzienda.getString("Nome");
                    } else {
                        System.out.println("Nessuna azienda trovata con la P.IVA specificata.");
                        return;
                    }
                }
            }

            // query per ottenere i corsi
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("\nCorsi a catalogo erogati dall'azienda " + nomeAzienda + ":");
                boolean hasResults = false;
                int i = 1;
                while (resultSet.next()) {
                    String titolo = resultSet.getString("Titolo");
                    String descrizione = resultSet.getString("Descrizione");
                    int numOre = resultSet.getInt("NumOre");
                    String modalita = resultSet.getString("Modalita");
                    String settore = resultSet.getString("Settore");
                    String argomenti = resultSet.getString("Argomenti");
                    String tipoServizio = resultSet.getString("TipoServizio");
                    double costoAPersona = resultSet.getDouble("CostoAPersona");

                    System.out.println("-- Corso n." + i + ": ");
                    System.out.println("  Titolo: " + titolo);
                    System.out.println("  Descrizione: " + descrizione);
                    System.out.println("  Numero Ore: " + numOre);
                    System.out.println("  Modalità: " + modalita);
                    System.out.println("  Settore: " + settore);
                    System.out.println("  Argomenti: " + argomenti);
                    System.out.println("  Tipo Servizio: " + tipoServizio);
                    System.out.println("  Costo a Persona: " + costoAPersona);
                    System.out.println("-------------------------------");

                    i++;
                    hasResults = true;
                }

                if (!hasResults) {
                    System.out.println("Nessun corso trovato per l'azienda specificata.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 7. Stampa di tutte le aziende erogatrici non impegnate in corsi personalizzati
    public void query7() {
        String query = """
            SELECT DISTINCT Azienda.PIVA, Azienda.Nome, Azienda.Mission, Azienda.Ruolo, Azienda.Tipo
            FROM Azienda
            LEFT JOIN Dipendente ON Azienda.PIVA = Dipendente.Azienda
            LEFT JOIN Docente ON Dipendente.CF = Docente.CF
            WHERE (Azienda.Ruolo = 'Erogatrice' OR Azienda.Ruolo = 'Entrambe')
              AND Azienda.PIVA NOT IN (
                  SELECT DISTINCT Dipendente.Azienda
                  FROM Dipendente
                  JOIN Docente ON Dipendente.CF = Docente.CF
                  JOIN Gestione ON Docente.CF = Gestione.Docente
              )
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)) {
            System.out.println("\n-------------- Aziende non impegnate in corsi personalizzati --------------");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean hasResults = false;
                while (resultSet.next()) {
                    String piva = resultSet.getString("PIVA");
                    String nome = resultSet.getString("Nome");
                    String mission = resultSet.getString("Mission");
                    String ruolo = resultSet.getString("Ruolo");
                    String tipo = resultSet.getString("Tipo");

                    System.out.println("\nP.IVA: " + piva);
                    System.out.println("Nome: " + nome);
                    System.out.println("Mission: " + mission);
                    System.out.println("Ruolo: " + ruolo);
                    System.out.println("Tipo: " + tipo);
                    System.out.println("--------------------------------------------");
                    hasResults = true;
                }

                if (!hasResults) {
                    System.out.println("Nessuna azienda trovata con i criteri specificati.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 8. Verifica della possibilità di assegnare un docente a un corso
    public void query8() {
        String query = """
            SELECT COUNT(*) AS NumCorsi
            FROM Gestione
            WHERE Docente = ?
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)) {
            System.out.println("\n-------------- Verifica possibilità di assegnare un docente ad un corso --------------");
            System.out.print("Inserisci il CF del docente: ");
            String cfDocente = scanner.nextLine();
            preparedStatement.setString(1, cfDocente);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int numCorsi = resultSet.getInt("NumCorsi");

                    if (numCorsi < 3) {
                        System.out.println("Sì, è possibile assegnargli un nuovo corso. Attualmente gestisce " + numCorsi + " corsi.");
                    } else {
                        System.out.println("No, il docente è già impegnato in 3 corsi personalizzati.");
                    }
                } else {
                    System.out.println("Docente non trovato nella tabella Gestione.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 9. Verifica l’eventuale presenza di docenti attualmente non coinvolti in corsi
    public void query9() {
        String query = """
                SELECT Dipendente.CF, Dipendente.Nome, Dipendente.Cognome, Dipendente.Azienda
                FROM Dipendente
                JOIN Docente ON Dipendente.CF = Docente.CF
                LEFT JOIN Gestione ON Docente.CF = Gestione.Docente
                LEFT JOIN Collaborazione ON Docente.CF = Collaborazione.Docente
                WHERE Gestione.Docente IS NULL
                  AND Collaborazione.Docente IS NULL
                ORDER BY Dipendente.Cognome, Dipendente.Nome;
                """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("\n-------------- Docenti non impegnati in corsi --------------");

                boolean hasResults = false;
                while (resultSet.next()) {
                    String cf = resultSet.getString("CF");
                    String nome = resultSet.getString("Nome");
                    String cognome = resultSet.getString("Cognome");
                    String azienda = resultSet.getString("Azienda");

                    System.out.println("- CF: " + cf + " | Nome: " + nome + " | Cognome: " + cognome + " | Azienda: " + azienda);
                    hasResults = true;
                }

                if (!hasResults) {
                    System.out.println("Nessun docente trovato che non sia impegnato in corsi.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 10. Per ciascun corso a catalogo, stampa il numero totale di discenti
    public void query10() {
        String query = """
            SELECT
                CorsoCatalogo.ID AS CorsoID,
                CorsoCatalogo.Titolo AS TitoloCorso,
                COALESCE(SUM(Iscrizione.NumDipendenti), 0) AS TotaleDiscenti
            FROM
                CorsoCatalogo
            LEFT JOIN Classe ON CorsoCatalogo.ID = Classe.CorsoCatalogo
            LEFT JOIN
                Iscrizione ON Classe.CorsoCatalogo = Iscrizione.CorsoClasse
                AND Classe.DataInizio = Iscrizione.DataInizioClasse
                AND Classe.DataFine = Iscrizione.DataFineClasse
            GROUP BY
                CorsoCatalogo.ID, CorsoCatalogo.Titolo
            ORDER BY
                CorsoCatalogo.ID;
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n-------------- Totale discenti per ciascun corso a catalogo --------------");

            while (resultSet.next()) {
                int corsoId = resultSet.getInt("CorsoID");
                String titoloCorso = resultSet.getString("TitoloCorso");
                int totaleDiscenti = resultSet.getInt("TotaleDiscenti");

                System.out.printf("Corso ID: %d | Titolo: %s | Totale Discenti: %d%n",
                        corsoId, titoloCorso, totaleDiscenti);
            }

        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 11. Stampa i dati del docente maggiormente impiegato in corsi (a catalogo e/o personalizzato)
    public void query11() {
        String query = """
            SELECT
                Dipendente.CF,
                Dipendente.Nome,
                Dipendente.Cognome,
                Dipendente.Azienda,
                COALESCE(G.TotaleGestione, 0) + COALESCE(C.TotaleCollaborazione, 0) AS TotalePartecipazioni
            FROM
                Dipendente
            JOIN
                Docente ON Dipendente.CF = Docente.CF
            LEFT JOIN
                (SELECT Docente, COUNT(*) AS TotaleGestione FROM Gestione GROUP BY Docente) AS G
                ON Docente.CF = G.Docente
            LEFT JOIN
                (SELECT Docente, COUNT(*) AS TotaleCollaborazione FROM Collaborazione GROUP BY Docente) AS C
                ON Docente.CF = C.Docente
            WHERE
                COALESCE(G.TotaleGestione, 0) + COALESCE(C.TotaleCollaborazione, 0) = (
                    SELECT MAX(COALESCE(G.TotaleGestione, 0) + COALESCE(C.TotaleCollaborazione, 0))
                    FROM
                        Docente
                    LEFT JOIN
                        (SELECT Docente, COUNT(*) AS TotaleGestione FROM Gestione GROUP BY Docente) AS G
                        ON Docente.CF = G.Docente
                    LEFT JOIN
                        (SELECT Docente, COUNT(*) AS TotaleCollaborazione FROM Collaborazione GROUP BY Docente) AS C
                        ON Docente.CF = C.Docente
                );
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n-------------- Docente maggiormente impiegato --------------");

            while (resultSet.next()) {
                String cf = resultSet.getString("CF");
                String nome = resultSet.getString("Nome");
                String cognome = resultSet.getString("Cognome");
                String azienda = resultSet.getString("Azienda");
                int totalePartecipazioni = resultSet.getInt("TotalePartecipazioni");

                System.out.printf("- CF: %s | Nome: %s | Cognome: %s | Azienda: %s | Totale Impieghi: %d%n",
                        cf, nome, cognome, azienda, totalePartecipazioni);
            }

        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 12. Stampa di tutti i corsi a catalogo per i quali non si è mai formata più di una classe
    public void query12() {
        String query = """
            SELECT CorsoCatalogo.*,
                   COALESCE(ClassiCount.NumeroClassi, 0) AS NumeroClassi
            FROM CorsoCatalogo
            LEFT JOIN (
                SELECT CorsoCatalogo, COUNT(*) AS NumeroClassi
                FROM Classe
                GROUP BY CorsoCatalogo
            ) AS ClassiCount ON CorsoCatalogo.ID = ClassiCount.CorsoCatalogo
            WHERE COALESCE(ClassiCount.NumeroClassi, 0) <= 1
            ORDER BY CorsoCatalogo.ID;
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n------ Corsi a catalogo con massimo 1 classe associata ------");
            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String titolo = resultSet.getString("Titolo");
                String descrizione = resultSet.getString("Descrizione");
                int numOre = resultSet.getInt("NumOre");
                String modalita = resultSet.getString("Modalita");
                String settore = resultSet.getString("Settore");
                String tipoServizio = resultSet.getString("TipoServizio");
                double costoAPersona = resultSet.getDouble("CostoAPersona");
                int numeroClassi = resultSet.getInt("NumeroClassi");

                System.out.printf("""
                        ID: %d
                        Titolo: %s
                        Descrizione: %s
                        Numero Ore: %d
                        Modalità: %s
                        Settore: %s
                        Tipologia Servizio: %s
                        Costo a Persona: %.2f
                        Numero Classi: %d
                        ---------------------------------
                        """,
                        id, titolo, descrizione, numOre, modalita, settore, tipoServizio, costoAPersona, numeroClassi);
            }

        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 13. Stampa dei dati delle aziende erogatrici, compreso il ricavo totale
    public void query13() {
        String query = """
            SELECT Azienda.*,
                   COALESCE(RicaviClassi.RicavoTotaleClassi, 0) + COALESCE(RicaviPersonalizzati.RicavoTotalePersonalizzati, 0) AS RicavoTotale
            FROM Azienda
            LEFT JOIN (
                SELECT Classe.Azienda, SUM(Classe.RicavoClasse) AS RicavoTotaleClassi
                FROM Classe
                GROUP BY Classe.Azienda
            ) AS RicaviClassi ON Azienda.PIVA = RicaviClassi.Azienda
            LEFT JOIN (
                SELECT Dipendente.Azienda, SUM(CorsoPersonalizzato.CostoTotale) AS RicavoTotalePersonalizzati
                FROM Dipendente
                JOIN Docente ON Dipendente.CF = Docente.CF
                JOIN Gestione ON Docente.CF = Gestione.Docente
                JOIN CorsoPersonalizzato ON Gestione.CorsoPersonalizzato = CorsoPersonalizzato.ID
                GROUP BY Dipendente.Azienda
            ) AS RicaviPersonalizzati ON Azienda.PIVA = RicaviPersonalizzati.Azienda
            WHERE (Azienda.Ruolo = 'Erogatrice' OR Azienda.Ruolo = 'Entrambe')
            ORDER BY RicavoTotale DESC;
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n------ Aziende erogatrici e ricavi totali ------");
            while (resultSet.next()) {
                String piva = resultSet.getString("PIVA");
                String nome = resultSet.getString("Nome");
                String mission = resultSet.getString("Mission");
                String ruolo = resultSet.getString("Ruolo");
                String tipo = resultSet.getString("Tipo");
                int numDipendenti = resultSet.getInt("NumDipendenti");
                double ricavoTotale = resultSet.getDouble("RicavoTotale");

                System.out.printf("""
                    PIVA: %s
                    Nome: %s
                    Mission: %s
                    Ruolo: %s
                    Tipo: %s
                    Numero dipendenti: %d
                    Ricavo Totale: %.2f
                    ---------------------------------
                    """, piva, nome, mission, ruolo, tipo, numDipendenti, ricavoTotale);
            }

        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 14. Stampa di ogni classe, compreso il ricavo ottenuto mediante la definizione della stessa
    public void query14() {
        String query = """
            SELECT *
            FROM Classe
            ORDER BY CorsoCatalogo, DataInizio;
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n------ Dettagli delle classi e relativi ricavi ------");
            while (resultSet.next()) {
                int corsoCatalogo = resultSet.getInt("CorsoCatalogo");
                Date dataInizio = resultSet.getDate("DataInizio");
                Date dataFine = resultSet.getDate("DataFine");
                Date dataScadenzaIscrizioni = resultSet.getDate("DataScadenzaIscrizioni");
                double ricavoClasse = resultSet.getDouble("RicavoClasse");
                String azienda = resultSet.getString("Azienda");

                System.out.printf("""
                    ID corso erogato: %d
                    Data Inizio: %s
                    Data Fine: %s
                    Data Scadenza Iscrizioni: %s
                    Ricavo Classe: %.2f
                    Azienda erogatrice: %s
                    ---------------------------------
                    """, corsoCatalogo, dataInizio, dataFine, dataScadenzaIscrizioni, ricavoClasse, azienda);
            }

        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // 15. Stampa una classifica delle aziende fruitrici sulla base del numero di servizi che ha richiesto
    public void query15() {
        String query = """
            SELECT
                Azienda.PIVA,
                Azienda.Nome,
                COALESCE(RichiesteCount.NumeroRichieste, 0) AS NumeroRichieste,
                COALESCE(IscrizioniCount.NumeroIscrizioni, 0) AS NumeroIscrizioni,
                COALESCE(RichiesteCount.NumeroRichieste, 0) + COALESCE(IscrizioniCount.NumeroIscrizioni, 0) AS TotaleServizi
            FROM Azienda
            LEFT JOIN (
                SELECT Azienda, COUNT(*) AS NumeroRichieste
                FROM Richiesta
                GROUP BY Azienda
            ) AS RichiesteCount ON Azienda.PIVA = RichiesteCount.Azienda
            LEFT JOIN (
                SELECT Azienda, COUNT(*) AS NumeroIscrizioni
                FROM Iscrizione
                GROUP BY Azienda
            ) AS IscrizioniCount ON Azienda.PIVA = IscrizioniCount.Azienda
            WHERE (Azienda.Ruolo = 'Fruitrice' OR Azienda.Ruolo = 'Entrambe')
            ORDER BY TotaleServizi DESC, Azienda.Nome;
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n------ Classifica Aziende Fruitrici ------");

            int posClassifica = 1;
            while (resultSet.next()) {
                String piva = resultSet.getString("PIVA");
                String nome = resultSet.getString("Nome");
                int numeroRichieste = resultSet.getInt("NumeroRichieste");
                int numeroIscrizioni = resultSet.getInt("NumeroIscrizioni");
                int totaleServizi = resultSet.getInt("TotaleServizi");

                System.out.printf("""
                    %d.
                    PIVA: %s
                    Nome: %s
                    Numero Richieste: %d
                    Numero Iscrizioni: %d
                    Totale Servizi: %d
                    ---------------------------------
                    """, posClassifica, piva, nome, numeroRichieste, numeroIscrizioni, totaleServizi);
                posClassifica++;
            }

        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Metodo per eliminare un corso personalizzato in caso di errore
    private void eliminaCorsoP(int idCorso) {
        String query = "DELETE FROM CorsoPersonalizzato WHERE ID = ?";

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)) {
            // Imposta il valore del parametro ID
            preparedStatement.setInt(1, idCorso);

            int righeEliminate = preparedStatement.executeUpdate();

            if (righeEliminate > 0) {
                System.out.println("Corso personalizzato eliminato con successo!");
            } else {
                System.out.println("Nessun corso personalizzato trovato con l'ID specificato.");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // Metodo per gestire le SQLException
    private void handleSQLException(SQLException e) {
        // Verifica se l'errore è causato dal trigger RV1
        if (e.getSQLState().equals("45000")) {
            System.out.println("Errore: Il docente è già assegnato a 3 corsi personalizzati.");
            // Verifica se l'errore è causato dal trigger RV3
        } else if (e.getSQLState().equals("45001")) {
            System.out.println("Errore: L'azienda ha superato la spesa massima di 40000 euro.");
        } else {
            // In caso di errore generico, stampa il messaggio di errore
            System.out.println("Errore nella comunicazione con il database: " + e.getMessage());
        }
    }
}
