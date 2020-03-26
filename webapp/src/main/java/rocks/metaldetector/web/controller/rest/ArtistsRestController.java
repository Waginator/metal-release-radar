package rocks.metaldetector.web.controller.rest;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rocks.metaldetector.config.constants.Endpoints;
import rocks.metaldetector.discogs.facade.dto.DiscogsArtistSearchResultDto;
import rocks.metaldetector.service.artist.ArtistsService;

@RestController
@RequestMapping(Endpoints.Rest.ARTISTS)
@AllArgsConstructor
public class ArtistsRestController {

  private final ArtistsService artistsService;

  @GetMapping(path = Endpoints.Rest.SEARCH,
              produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<DiscogsArtistSearchResultDto> handleNameSearch(@RequestParam(value = "query", defaultValue = "") String query,
                                                                       @PageableDefault Pageable pageable) {
    DiscogsArtistSearchResultDto result = artistsService.searchDiscogsByName(query, pageable);
    return ResponseEntity.ok(result);
  }

  @PostMapping(path = Endpoints.Rest.FOLLOW + "/{discogsId}")
  public ResponseEntity<Void> handleFollow(@PathVariable long discogsId) {
    artistsService.followArtist(discogsId);
    return ResponseEntity.ok().build();
  }

  @PostMapping(path = Endpoints.Rest.UNFOLLOW + "/{discogsId}")
  public ResponseEntity<Void> handleUnfollow(@PathVariable long discogsId) {
    artistsService.unfollowArtist(discogsId);
    return ResponseEntity.ok().build();
  }
}
