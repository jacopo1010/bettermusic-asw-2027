package asw.bettermusic.recensioni.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class RecensioneCreataEvent implements RecensioniDomainEvent {
	
	private Long id; 
	private String recensore; 
	private Long idAlbum; 
	private String testo; 
	private String sunto; 

}
