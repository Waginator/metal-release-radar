package rocks.metaldetector.service.telegram;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rocks.metaldetector.persistence.domain.notification.TelegramConfigEntity;
import rocks.metaldetector.persistence.domain.notification.TelegramConfigRepository;
import rocks.metaldetector.persistence.domain.user.AbstractUserEntity;
import rocks.metaldetector.service.notification.config.TelegramConfigService;
import rocks.metaldetector.telegram.facade.TelegramMessagingService;
import rocks.metaldetector.web.api.request.TelegramUpdate;

import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class TelegramServiceImpl implements TelegramService {

  static final String FIRST_BOT_MESSAGE_TEXT = "/start";
  static final String FIRST_BOT_RESPONSE_TEXT = "Hi! If you want to register for notifications, please generate a " +
                                                "registration ID on metal-detector.rocks and send it to me here!";

  private final TelegramConfigService telegramConfigService;
  private final TelegramConfigRepository telegramConfigRepository;
  private final TelegramMessagingService telegramMessagingService;

  @Override
  public void processUpdate(TelegramUpdate update) {
    if (isFirstBotMessage(update)) {
      sendFirstMessageResponse(update);
    }
    else {
      registerForTelegramNotifications(update);
    }
  }

  @Override
  public void sendMessage(AbstractUserEntity user, String message) {
    Optional<TelegramConfigEntity> telegramConfigOptional = telegramConfigRepository.findByUser(user);

    if (telegramConfigOptional.isPresent()) {
      TelegramConfigEntity telegramConfig = telegramConfigOptional.get();
      var chatId = telegramConfig.getChatId();
      if (chatId != null) {
        telegramMessagingService.sendMessage(chatId, message);
      }
    }
  }

  private void registerForTelegramNotifications(TelegramUpdate update) {
    String messageText = update.getMessage().getText();
    int chatId = update.getMessage().getChat().getId();
    telegramConfigService.updateChatId(messageText, chatId);
  }

  private boolean isFirstBotMessage(TelegramUpdate update) {
    return update.getMessage().getText().equals(FIRST_BOT_MESSAGE_TEXT);
  }

  private void sendFirstMessageResponse(TelegramUpdate update) {
    int chatId = update.getMessage().getChat().getId();
    telegramMessagingService.sendMessage(chatId, FIRST_BOT_RESPONSE_TEXT);
  }
}
