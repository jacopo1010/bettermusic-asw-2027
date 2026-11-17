package asw.bettermusic.album.api.event;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* Evento Album Creato */
@Data @NoArgsConstructor @AllArgsConstructor
public class AlbumCreatoEvent implements AlbumDomainEvent {
    private Long id; 
	private String titolo; 
	private String artista; 
	private Set<String> generi; 
}
