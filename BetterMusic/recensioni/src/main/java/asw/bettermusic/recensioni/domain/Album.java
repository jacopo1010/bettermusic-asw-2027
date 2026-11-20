package asw.bettermusic.recensioni.domain;

import jakarta.persistence.*;

import lombok.*;

import java.util.*;

/* Replica locale di un album, aggiornata dagli eventi pubblicati dal servizio album. */
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Album {

	/* id dell'album (assegnato dal servizio album, non generato qui) */
	@Id
	private Long id;
	/* il titolo dell'album */
	private String titolo;
	/* artista autore dell'album */
	private String artista;
	/* generi dell'album */
	@ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
	private Set<String> generi;

}
