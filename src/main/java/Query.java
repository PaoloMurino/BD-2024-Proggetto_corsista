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

            // Esecuzione della query
            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Corso a catalogo inserito con successo! --------------");
            } else {
                System.out.println("\n-------------- Errore durante l'inserimento del corso. --------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

    // 2. Iscrizione di un'azienda a una classe
    public void query2() {
        String query = "INSERT INTO Iscrizione (Azienda, DataInizioClasse, DataFineClasse, CorsoClasse, " +
                "NumDipendenti) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)) {
            System.out.println("\n-------------- Iscrizione di un'Azienda ad una classe! --------------");
            System.out.print("Inserisci la P.IVA dell'Azienda: ");
            String azienda = scanner.nextLine();
            preparedStatement.setString(1, azienda);

            System.out.print("Inserisci il corso erogato dalla classe: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int corsoClasse = scanner.nextInt();
            preparedStatement.setInt(4, corsoClasse);
            scanner.nextLine(); // Pulizia del buffer

            System.out.print("Inserisci la data di inizio della classe (formato YYYY-MM-DD): ");
            String dataInizioInput = scanner.nextLine();
            Date dataInizio = Date.valueOf(dataInizioInput);
            preparedStatement.setDate(2, dataInizio);

            System.out.print("Inserisci la data di fine della classe (formato YYYY-MM-DD): ");
            String dataFineInput = scanner.nextLine();
            Date dataFine = Date.valueOf(dataFineInput);
            preparedStatement.setDate(3, dataFine);

            System.out.print("Inserisci il numero di dipendenti da iscrivere: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int numDipendenti = scanner.nextInt();
            preparedStatement.setInt(5, numDipendenti);
            scanner.nextLine(); // Pulizia del buffer

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
                    //System.out.println("Ricavo Classe aggiornato con successo!");
                } else {
                    System.out.println("Errore durante l'aggiornamento di Ricavo Classe.");
                }
            }

            // Esecuzione della query
            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Azienda iscritta con successo! --------------");
            } else {
                System.out.println("\n-------------- Errore durante l'iscrizione dell'Azienda. --------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

    // 3. Richiesta Corso personalizzato
    public void query3() {
        String query = "INSERT INTO CorsoPersonalizzato (Titolo, Descrizione, NumOre, Modalita, TipoServizio, DataInizio, " +
                "DataFine, TestDMA, NumeroTrasferte, CostoTotale) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            String dataInizioInput = scanner.nextLine();
            Date dataInizio = Date.valueOf(dataInizioInput);
            preparedStatement.setDate(6, dataInizio);

            System.out.print("Inserisci la data di fine del corso (formato YYYY-MM-DD): ");
            String dataFineInput = scanner.nextLine();
            Date dataFine = Date.valueOf(dataFineInput);
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
                        int idCorsoPersonalizzato = generatedKeys.getInt(1); // ID del corso

                        // Creazione della richiesta
                        String queryRichiesta = "INSERT INTO Richiesta (Azienda, DataRichiesta, CorsoPersonalizzato) VALUES (?, ?, ?)";
                        try (PreparedStatement richiestaStatement = database.getConnection().prepareStatement(queryRichiesta)) {
                            System.out.print("Inserisci la P.IVA dell'Azienda che ha effettuato la richiesta: ");
                            String azienda = scanner.nextLine();
                            richiestaStatement.setString(1, azienda);

                            // Imposta la data odierna
                            Date dataRichiesta = Date.valueOf(LocalDate.now());
                            richiestaStatement.setDate(2, dataRichiesta);

                            // Imposta l'ID del corso personalizzato
                            richiestaStatement.setInt(3, idCorsoPersonalizzato);

                            // Esecuzione della query per creare la richiesta
                            int tupleInseriteRichiesta = richiestaStatement.executeUpdate();
                            if (tupleInseriteRichiesta > 0) {
                                System.out.println("\n-------------- Richiesta creata con successo! --------------");
                            } else {
                                System.out.println("\n-------------- Errore durante la creazione della richiesta! --------------");
                            }
                        }

                        String queryGestione = "INSERT INTO Gestione (Docente, CorsoPersonalizzato) VALUES (?, ?)";
                        try (PreparedStatement gestioneStatement = database.getConnection().prepareStatement(queryGestione)) {
                            System.out.print("Inserisci il CF del docente che gestisce il corso: ");
                            String docente = scanner.nextLine();
                            gestioneStatement.setString(1, docente);

                            // Imposta l'ID del corso personalizzato
                            gestioneStatement.setInt(2, idCorsoPersonalizzato);

                            // Esecuzione della query per creare la richiesta
                            int tupleInseriteGestione = gestioneStatement.executeUpdate();
                            if (tupleInseriteGestione > 0) {
                                System.out.println("\n-------------- Gestione creata con successo! --------------");
                            } else {
                                System.out.println("\n-------------- Errore durante la creazione della gestione! --------------");
                            }
                        }
                    } else {
                        System.out.println("Errore: Impossibile ottenere l'ID del corso personalizzato appena creato!");
                    }
                }
            } else {
                System.out.println("\n-------------- Errore durante la creazione del corso. --------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

    public void query4() {
        String query = "INSERT INTO Collaborazione (Docente, CorsoClasse, DataInizioClasse, DataFineClasse)  " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query)) {
            System.out.println("\n-------------- Aggiunta di un nuovo docente ad una classe! --------------");
            System.out.print("Inserisci il CF del docente: ");
            String docente = scanner.nextLine();
            preparedStatement.setString(1, docente);

            System.out.print("Inserisci il corso erogato dalla classe: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int corsoClasse = scanner.nextInt();
            preparedStatement.setInt(2, corsoClasse);
            scanner.nextLine(); // Pulizia del buffer

            System.out.print("Inserisci la data di inizio della classe (formato YYYY-MM-DD): ");
            String dataInizioInput = scanner.nextLine();
            Date dataInizio = Date.valueOf(dataInizioInput);
            preparedStatement.setDate(3, dataInizio);

            System.out.print("Inserisci la data di fine della classe (formato YYYY-MM-DD): ");
            String dataFineInput = scanner.nextLine();
            Date dataFine = Date.valueOf(dataFineInput);
            preparedStatement.setDate(4, dataFine);

            // Esecuzione della query
            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Docente aggiunto con successo! --------------");
            } else {
                System.out.println("\n-------------- Errore durante l'aggiunta del docente. --------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

    public void query5() {
        String updateQuery = """
                UPDATE Gestione
                SET Docente = ?
                WHERE Gestione.CorsoPersonalizzato = ?
            """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(updateQuery)) {
            System.out.println("\n-------------- Modifica docente di un Corso personalizzato! --------------");

            System.out.print("Inserisci il corso di cui si vuole modificare il docente: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Errore: Inserisci un numero valido!");
                scanner.next();
            }
            int corsoPersonalizzato = scanner.nextInt();
            preparedStatement.setInt(2, corsoPersonalizzato);
            scanner.nextLine(); // Pulizia del buffer

            System.out.print("Inserisci il nuovo docente: ");
            String docente = scanner.nextLine();
            preparedStatement.setString(1, docente);

            // Esecuzione della query
            int tupleInserite = preparedStatement.executeUpdate();
            if (tupleInserite > 0) {
                System.out.println("\n-------------- Docente modificato con successo! --------------");
            } else {
                System.out.println("\n-------------- Errore durante la modifica del docente. --------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

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
            String nomeAzienda = null;

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

            // Esegui la query per ottenere i corsi
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
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

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
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

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
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

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

                    System.out.println("- CF: " + cf + ", Nome: " + nome + ", Cognome: " + cognome + ", Azienda: " + azienda);
                    hasResults = true;
                }

                if (!hasResults) {
                    System.out.println("Nessun docente trovato che non sia impegnato in corsi.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

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
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }

    public void query11() {
        String query = """
            SELECT 
                Dipendente.CF, 
                Dipendente.Nome, 
                Dipendente.Cognome, 
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
            ORDER BY 
                TotalePartecipazioni DESC
            LIMIT 1;
        """;

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("\n-------------- Docente maggiormente impiegato --------------");

            if (resultSet.next()) {
                String cf = resultSet.getString("CF");
                String nome = resultSet.getString("Nome");
                String cognome = resultSet.getString("Cognome");
                int totalePartecipazioni = resultSet.getInt("TotalePartecipazioni");

                System.out.printf("CF: %s | Nome: %s | Cognome: %s | Totale Partecipazioni: %d%n",
                        cf, nome, cognome, totalePartecipazioni);
            } else {
                System.out.println("Nessun docente trovato.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore nella comunicazione con il database.");
        }
    }
}
