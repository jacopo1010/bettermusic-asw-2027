package asw.bettermusic.recensioniseguite.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.*; 

/* Repository della replica locale delle connessioni. */
public interface ConnessioneRepository extends CrudRepository<Connessione, Long> {

	Collection<Connessione> findByUtente(String utente);

	Collection<Connessione> findByUtenteAndRuolo(String utente, String ruolo);

}
