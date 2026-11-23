package asw.bettermusic.recensioni.domain;

import org.springframework.stereotype.Service;

import asw.bettermusic.recensioni.api.event.RecensioneCreataEvent;
import asw.bettermusic.recensioni.api.event.RecensioniDomainEvent;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/* Implementazione "Kafka-based": crea le recensioni usando la replica locale degli album (nessuna chiamata REST). */
@Service("RecensioniServiceKafkaBasedImpl")
public class RecensioniServiceKafkaBasedImpl implements RecensioniService {

	@Autowired
	private RecensioniRepository recensioniRepository;

	@Autowired
	private AlbumReplicaRepository albumReplicaRepository;

	@Autowired
	private RecensioniEventPublisher recensioniEventPublisher;

	/* Crea una nuova recensione, a partire dai suoi dati. */
 	public Recensione createRecensione(String recensore, String titoloAlbum, String artistaAlbum, String testo, String sunto) {
		Album album = albumReplicaRepository.findByTitoloAndArtista(titoloAlbum, artistaAlbum).orElse(null);
		Recensione recensione = new Recensione(recensore, album.getId(), testo, sunto);
		recensione = recensioniRepository.save(recensione);
		/* pubblicazione dell'evento (dopo il salvataggio, così l'id è valorizzato) */
		RecensioniDomainEvent event = new RecensioneCreataEvent(recensione.getId(),recensione.getRecensore(),recensione.getIdAlbum(),recensione.getTesto(),recensione.getSunto());
		this.recensioniEventPublisher.publish(event);
		return recensione;
	}

	/* Trova una recensione, dato l'id. */
 	public Recensione getRecensione(Long id) {
		Recensione recensione = recensioniRepository.findById(id).orElse(null);
		return recensione;
	}

	/* Trova tutte le recensioni. */
	public Collection<Recensione> getRecensioni() {
		Collection<Recensione> recensioni = recensioniRepository.findAll();
		return recensioni;
	}

	/* Trova tutte le recensioni di un album, dato l'id. */
	public Collection<Recensione> getRecensioniByIdAlbum(Long idAlbum) {
		Collection<Recensione> recensioni = recensioniRepository.findByIdAlbum(idAlbum);
		return recensioni;
	}

	/* Trova tutte le recensioni scritte da un recensore. */
	public Collection<Recensione> getRecensioniByRecensore(String recensore) {
		Collection<Recensione> recensioni = recensioniRepository.findByRecensore(recensore);
		return recensioni;
	}

}
