package asw.bettermusic.recensioniseguite.domain;

import jakarta.persistence.*;

import lombok.*; 

/* Replica locale di una recensione (in formato breve), aggiornata dagli eventi del servizio recensioni. 
 * Una recensione nel dominio delle recensioni-seguite 
 * corrisponde in effetti ad una recensione "breve" nel dominio delle recensioni. */ 
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Recensione implements Comparable<Recensione> {

	/* id della recensione (assegnato dal servizio recensioni) */
	@Id
	@EqualsAndHashCode.Include
	private Long id; 
	/* chi ha scritto la recensione */ 
	private String recensore; 
	/* id dell'album oggetto della recensione */ 
	private Long idAlbum; 
	/* sunto del testo della recensione */ 
	private String sunto; 

	@Override
	public int compareTo(Recensione other) {
		return this.id.compareTo(other.id); 
	}
	
}
