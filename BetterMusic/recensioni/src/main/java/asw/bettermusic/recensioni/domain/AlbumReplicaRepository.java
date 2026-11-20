package asw.bettermusic.recensioni.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.*;

/* Repository della replica locale degli album. */
public interface AlbumReplicaRepository extends CrudRepository<Album, Long> {

	Optional<Album> findByTitoloAndArtista(String titolo, String artista);

}
