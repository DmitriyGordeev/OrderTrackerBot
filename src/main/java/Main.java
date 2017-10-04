import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {

    public static void main(String[] args) {

        System.out.println("Bot has been launched!");

        // Initialize Api Context
        ApiContextInitializer.init();

        // Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // Register our bot
        try {
            botsApi.registerBot(new Bot("sales_release"));
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

}
