import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;


public class SimpleBot extends TelegramLongPollingBot {

    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            System.out.println(update.getMessage().getText());

            String username = update.getMessage().getChat().getUserName();
            long userId   = update.getMessage().getChat().getId();
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            message_text += "\n username: " + username + " \n userId: " + userId;

            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(message_text);
            try {
                sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    public String getBotUsername() {
        return "dimas_test_bot";
    }

    public String getBotToken() {
        return "415598199:AAHFEgk3tjCeQmZ7YDBmH90XC0PYN0PyuSw";
    }
}