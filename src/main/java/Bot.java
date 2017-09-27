import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Bot extends TelegramLongPollingBot {

    private void sendDocUploadingAFile(Long chatId, java.io.File save,String caption) throws TelegramApiException {

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setNewDocument(save);
        sendDocumentRequest.setCaption(caption);
        sendDocument(sendDocumentRequest);
    }

    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            /// Std staff:
            System.out.println(update.getMessage().getText());

            String username = update.getMessage().getChat().getUserName();
            long userId   = update.getMessage().getChat().getId();
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();





            /// Reading associated file:
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat dateFormatDateOnly = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();


            // get file command:
            if(message_text.equals("/getfile")) {
                File file = new File(dateFormatDateOnly.format(date) + ".csv");
                if(file.exists()) {
                    try {
                        sendDocUploadingAFile(chat_id, file, "Статистика за сегодня:\n ");
                    }
                    catch(TelegramApiException e) { e.printStackTrace(); }
                }
            }


            String record = userId + ";" + username + ";" + message_text + ";" + dateFormat.format(date) + "\n";

            String fileContent = "";
            try {
                fileContent = Fileio.readfile(dateFormatDateOnly.format(date) + ".csv");
                fileContent = fileContent + record;
                Fileio.writefile(dateFormatDateOnly.format(date) + ".csv", fileContent);
            }
            catch(IOException e) {
                e.printStackTrace();
                try {
                    Fileio.writefile(dateFormatDateOnly.format(date) + ".csv", record);
                }
                catch(IOException exc) {
                    exc.printStackTrace();
                }
            }

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
        return "OrderTrackerBot_bot";
    }

    public String getBotToken() {
        return "445107190:AAGSZJHeTLrzcq2AAFGVuMn20C1xEFU6A5U";
    }
}