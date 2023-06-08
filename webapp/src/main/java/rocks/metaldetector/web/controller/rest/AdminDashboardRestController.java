package rocks.metaldetector.web.controller.rest;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rocks.metaldetector.service.admin.dashboard.AdminDashboardService;
import rocks.metaldetector.web.api.response.AdminDashboardResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static rocks.metaldetector.support.Endpoints.Rest.ADMIN_DASHBOARD;

@RestController
public class AdminDashboardRestController {

  private final AdminDashboardService adminDashboardService;

  public AdminDashboardRestController(@Qualifier("adminDashboardServiceImpl") AdminDashboardService adminDashboardService) {
    this.adminDashboardService = adminDashboardService;
  }

  @GetMapping(path = ADMIN_DASHBOARD, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<AdminDashboardResponse> handleAdminDashboardRequest() {
    AdminDashboardResponse adminDashboardResponse = adminDashboardService.createAdminDashboardResponse();
    return ResponseEntity.ok(adminDashboardResponse);
  }
}
