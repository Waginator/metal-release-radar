package rocks.metaldetector.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rocks.metaldetector.testutil.WithIntegrationTestConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static rocks.metaldetector.support.Endpoints.Rest.TEST;

@SpringBootTest
@DirtiesContext
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest implements WithIntegrationTestConfig {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("All rest controller behind '/rest/**' are secured by default and return a 401")
  @WithAnonymousUser
  void test_rest_endpoint_security() throws Exception {
    mockMvc.perform(get(TEST))
           .andExpect(status().isUnauthorized());
  }

  // ToDo NilsD remove?
//  @Test
//  @DisplayName("All mvc controller are secured by default and redirect to login page")
//  @WithAnonymousUser
//  void test_mvc_endpoint_security() throws Exception {
//    mockMvc.perform(get(Endpoints.Frontend.TEST))
//            .andExpect(status().is3xxRedirection())
//            .andExpect(header().string(LOCATION, containsString(LOGIN)));
//  }
}

@RestController
class SimpleRestController {

  @GetMapping(TEST)
  ResponseEntity<String> test() {
    return ResponseEntity.ok("test");
  }
}
