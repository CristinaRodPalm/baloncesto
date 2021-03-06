package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.FavouritePlayer;
import com.mycompany.myapp.domain.Player;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.FavouritePlayerRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.FavouritePlayerService;
import com.mycompany.myapp.service.dto.PlayerDTO;
import com.mycompany.myapp.web.rest.util.HeaderUtil;
import com.mycompany.myapp.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing FavouritePlayer.
 */
@RestController
@RequestMapping("/api")
public class FavouritePlayerResource {

    private final Logger log = LoggerFactory.getLogger(FavouritePlayerResource.class);

    @Inject
    private FavouritePlayerService favouritePlayerService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private FavouritePlayerRepository favouritePlayerRepository;

    // POST
    @PostMapping("/favourite-players")
    @Timed
    public ResponseEntity<FavouritePlayer> createFavouritePlayer(@RequestBody FavouritePlayer favouritePlayer) throws URISyntaxException {
        log.debug("REST request to save FavouritePlayer : {}", favouritePlayer);
        if (favouritePlayer.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("favouritePlayer", "idexists", "A new favouritePlayer cannot already have an ID")).body(null);
        }

        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        ZonedDateTime now = ZonedDateTime.now();

        favouritePlayer.setUser(user);
        favouritePlayer.setFavouriteDateTime(now);

        FavouritePlayer result = favouritePlayerService.save(favouritePlayer);
        return ResponseEntity.created(new URI("/api/favourite-players/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("favouritePlayer", result.getId().toString()))
            .body(result);
    }

    // PUT
    @PutMapping("/favourite-players")
    @Timed
    public ResponseEntity<FavouritePlayer> updateFavouritePlayer(@RequestBody FavouritePlayer favouritePlayer) throws URISyntaxException {
        log.debug("REST request to update FavouritePlayer : {}", favouritePlayer);
        if (favouritePlayer.getId() == null) {
            return createFavouritePlayer(favouritePlayer);
        }
        FavouritePlayer result = favouritePlayerService.save(favouritePlayer);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("favouritePlayer", favouritePlayer.getId().toString()))
            .body(result);
    }

    // GET
    @GetMapping("/favourite-players")
    @Timed
    public ResponseEntity<List<FavouritePlayer>> getAllFavouritePlayers(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of FavouritePlayers");
        Page<FavouritePlayer> page = favouritePlayerService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/favourite-players");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    // GET by id
    @GetMapping("/favourite-players/{id}")
    @Timed
    public ResponseEntity<FavouritePlayer> getFavouritePlayer(@PathVariable Long id) {
        log.debug("REST request to get FavouritePlayer : {}", id);
        FavouritePlayer favouritePlayer = favouritePlayerService.findOne(id);
        return Optional.ofNullable(favouritePlayer)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // GET top players
    @GetMapping("/top-players")
    @Timed
    public ResponseEntity<List<PlayerDTO>> getTopPlayers()
        throws URISyntaxException {

        log.debug("REST request to get TopPlayers");

        List<Object[]> topPlayers = favouritePlayerRepository.findTopPlayers();

        List<PlayerDTO> result = new ArrayList<>();

        topPlayers.forEach(
            topPlayer -> {
                PlayerDTO p = new PlayerDTO();
                p.setPlayer((Player) topPlayer[0]);
                p.setNumFavs((Long) topPlayer[1]);

                result.add(p);
            }

        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // GET top five
    @GetMapping("/top-five")
    @Timed
    public ResponseEntity<List<PlayerDTO>> getTopFivePlayers()
        throws URISyntaxException {

        log.debug("REST request to get TopFivePlayers");

        Pageable topFive = new PageRequest(0, 5);
        List<Object[]> topFivePlayers = favouritePlayerRepository.findTopFivePlayers(topFive);

        List<PlayerDTO> result = new ArrayList<>();

        topFivePlayers.forEach(
            topPlayer -> {
                PlayerDTO p = new PlayerDTO();
                p.setPlayer((Player) topPlayer[0]);
                p.setNumFavs((Long) topPlayer[1]);

                result.add(p);
            }

        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // DELETE
    @DeleteMapping("/favourite-players/{id}")
    @Timed
    public ResponseEntity<Void> deleteFavouritePlayer(@PathVariable Long id) {
        log.debug("REST request to delete FavouritePlayer : {}", id);
        favouritePlayerService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("favouritePlayer", id.toString())).build();
    }

}
