package rocks.metaldetector.web.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UpdateNotificationConfigRequest {

  private boolean notify;
  private boolean notificationAtReleaseDate;
  private boolean notificationAtAnnouncementDate;

  @Min(0)
  private int frequencyInWeeks;

  @NotBlank
  private String channel;
}
