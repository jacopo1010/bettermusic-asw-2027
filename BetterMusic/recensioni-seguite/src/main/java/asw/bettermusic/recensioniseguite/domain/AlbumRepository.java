package asw.bettermusic.recensioniseguite.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.*; 

/* Repository della replica locale degli album. */
public interface AlbumRepository extends CrudRepository<Album, Long> {

	Collection<Album> findAll();

	Collection<Album> findByArtista(String artista);

	@Query(value = "SELECT a FROM Album a JOIN a.generi g WHERE g = :genere")
	Collection<Album> findByGenere(String genere);

}
