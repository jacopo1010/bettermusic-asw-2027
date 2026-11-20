package asw.bettermusic.recensioniseguite.domain;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*; 
import java.util.stream.*; 

/* Implementazione "Kafka-based": calcola le recensioni seguite leggendo solo dalle repliche locali
 * (aggiornate dagli eventi), senza invocare gli altri servizi. */
@Service("RecensioniSeguiteServiceKafkaBasedImpl")
public class RecensioniSeguiteServiceKafkaBasedImpl implements RecensioniSeguiteService {

	@Autowired 
	private ConnessioneRepository connessioneRepository;

	@Autowired 
	private RecensioneRepository recensioneRepository;

	@Autowired 
	private AlbumRepository albumRepository;

	/* Trova le recensioni seguite da un utente, 
	 * ovvero le recensioni degli album degli artisti, dei recensori e dei generi musicali seguiti da quell'utente. */ 
	public Collection<Recensione> getRecensioniSeguite(String utente) {
		Collection<Recensione> recensioniSeguite = new TreeSet<>(); 
		
		Collection<Connessione> connessioni = connessioneRepository.findByUtente(utente); 

		/* recensioni dei recensori seguiti */ 
		Collection<String> recensoriSeguiti = 
			connessioni
				.stream()
				.filter(c -> c.getRuolo().equals("RECENSORE"))
				.map(c -> c.getSeguito())
				.collect(Collectors.toSet()); 
		for (String recensore : recensoriSeguiti) { 
			recensioniSeguite.addAll(recensioneRepository.findByRecensore(recensore));
		}

		/* recensioni degli album degli artisti seguiti */ 
		Collection<String> artistiSeguiti = 
			connessioni
				.stream()
				.filter(c -> c.getRuolo().equals("ARTISTA"))
				.map(c -> c.getSeguito())
				.collect(Collectors.toSet()); 
		Collection<Album> albumDiArtistiSeguiti = new HashSet<>(); 
		for (String artista : artistiSeguiti) {
			albumDiArtistiSeguiti.addAll(albumRepository.findByArtista(artista));
		}
		for (Album album : albumDiArtistiSeguiti) {
			recensioniSeguite.addAll(recensioneRepository.findByIdAlbum(album.getId()));
		}

		/* recensioni degli album dei generi seguiti */ 
		Collection<String> generiSeguiti = 
			connessioni
				.stream()
				.filter(c -> c.getRuolo().equals("GENERE"))
				.map(c -> c.getSeguito())
				.collect(Collectors.toSet()); 
		Collection<Album> albumDiGeneriSeguiti = new HashSet<>(); 
		for (String genere : generiSeguiti) {
			albumDiGeneriSeguiti.addAll(albumRepository.findByGenere(genere));
		}
		for (Album album : albumDiGeneriSeguiti) {
			recensioniSeguite.addAll(recensioneRepository.findByIdAlbum(album.getId()));
		}

		return recensioniSeguite; 
	}

}
