package com.metalr2.web.controller;

import com.metalr2.config.constants.Endpoints;
import com.metalr2.config.constants.ViewNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.metalr2.model.user.UserFactory;
import com.metalr2.security.WebSecurity;
import com.metalr2.service.user.UserService;
import com.metalr2.web.controller.mvc.SearchArtistsController;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchArtistsController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Tag("integration-test")
class SearchArtistsControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Requesting '" + Endpoints.Frontend.SEARCH_ARTISTS + "' should return the view to search artists")
  void get_should_return_search_artists_view() throws Exception {
    mockMvc.perform(get(Endpoints.Frontend.SEARCH_ARTISTS))
              .andExpect(status().isOk())
              .andExpect(view().name(ViewNames.Frontend.SEARCH_ARTISTS))
              .andExpect(model().size(0))
              .andExpect(content().contentType("text/html;charset=UTF-8"))
              .andExpect(content().string(containsString("Search")));
  }

}
