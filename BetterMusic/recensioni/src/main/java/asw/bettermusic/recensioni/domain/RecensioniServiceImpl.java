package asw.bettermusic.recensioni.domain;

import org.springframework.stereotype.Service;

import asw.bettermusic.recensioni.eventpublisher.RecensioniEventKafkaPublisher;
import asw.bettermusic.recensioni.producerevent.RecensioneCreataEvent;
import asw.bettermusic.recensioni.producerevent.RecensioniDomainEvent;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*; 

@Service
public class RecensioniServiceImpl implements RecensioniService {

	@Autowired
	private RecensioniRepository recensioniRepository;

	@Autowired
	private AlbumClientPort albumClient;
	
	@Autowired
	private RecensioniEventKafkaPublisher recensioniEventKafkaPublisher;

	/* Crea una nuova recensione, a partire dai suoi dati. */ 
 	public Recensione createRecensione(String recensore, String titoloAlbum, String artistaAlbum, String testo, String sunto) {
		Album album = albumClient.getAlbum(titoloAlbum, artistaAlbum);
		Recensione recensione = new Recensione(recensore, album.getId(),  testo, sunto); 
		/* annuncio un evento di tipo creazione recensione */
		RecensioniDomainEvent event = new RecensioneCreataEvent(recensione.getId(),recensione.getRecensore(),recensione.getIdAlbum(),recensione.getTesto(),recensione.getSunto());
		this.recensioniEventKafkaPublisher.publish(event);
		recensione = recensioniRepository.save(recensione);
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
