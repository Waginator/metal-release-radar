package com.metalr2.discogs.restclient;

import com.metalr2.discogs.config.DiscogsConfig;
import com.metalr2.discogs.model.artist.Artist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ArtistRestClient extends AbstractDiscogsRestClient {

  private static final String ARTIST_URL_FRAGMENT = "/artists/{artistId}";

  public ArtistRestClient(RestTemplate restTemplate, DiscogsConfig discogsConfig) {
    super(restTemplate, discogsConfig);
    getArtist(125246);
  }

  public void getArtist(long artistId) {
    ResponseEntity<Artist> responseEntity = restTemplate.getForEntity(discogsConfig.getRestBaseUrl() + ARTIST_URL_FRAGMENT,
                                                                      Artist.class,
                                                                      artistId);

    log.info("Status code value: " + responseEntity.getStatusCodeValue());
    log.info("HTTP Header 'ContentType': " + responseEntity.getHeaders().getContentType());

    System.out.println(responseEntity.getBody());
  }

}
