package asw.bettermusic.recensioniseguite.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.*; 

/* Repository della replica locale delle recensioni. */
public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

	Collection<Recensione> findByRecensore(String recensore);

	Collection<Recensione> findByIdAlbum(Long idAlbum);

}
