package asw.bettermusic.recensioni.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* Classe che verra usata una volta implementato il metodo DELETE /recensioni/id */

@Data @NoArgsConstructor @AllArgsConstructor
public class RecensioneCancellataEvent implements RecensioniDomainEvent {
	
	private Long id; 
	private String recensore; 
	private Long idAlbum; 
	private String testo; 
	private String sunto; 

}
