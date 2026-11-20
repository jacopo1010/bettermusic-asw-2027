package asw.bettermusic.recensioniseguite.domain;

import asw.bettermusic.album.api.event.AlbumCreatoEvent;
import asw.bettermusic.connessioni.api.event.ConnessioneCancellataEvent;
import asw.bettermusic.connessioni.api.event.ConnessioneCreataEvent;
import asw.bettermusic.recensioni.producerevent.RecensioneCreataEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/* Applica gli eventi di dominio alle repliche locali (upsert per id). */
@Service
public class ReplicheService {

	@Autowired
	private AlbumRepository albumRepository;

	@Autowired
	private RecensioneRepository recensioneRepository;

	@Autowired
	private ConnessioneRepository connessioneRepository;

	/* Aggiorna la replica degli album (creazione/aggiornamento). */
	public void onAlbumCreato(AlbumCreatoEvent event) {
		Album album = new Album(event.getId(), event.getTitolo(), event.getArtista(), event.getGeneri());
		this.albumRepository.save(album);
	}

	/* Aggiorna la replica delle recensioni (creazione/aggiornamento). */
	public void onRecensioneCreata(RecensioneCreataEvent event) {
		Recensione recensione = new Recensione(event.getId(), event.getRecensore(), event.getIdAlbum(), event.getSunto());
		this.recensioneRepository.save(recensione);
	}

	/* Aggiorna la replica delle connessioni (creazione/aggiornamento). */
	public void onConnessioneCreata(ConnessioneCreataEvent event) {
		Connessione connessione = new Connessione(event.getId(), event.getUtente(), event.getSeguito(), event.getRuolo());
		this.connessioneRepository.save(connessione);
	}

	/* Aggiorna la replica delle connessioni (cancellazione). */
	public void onConnessioneCancellata(ConnessioneCancellataEvent event) {
		this.connessioneRepository.deleteById(event.getId());
	}

}
