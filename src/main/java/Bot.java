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

    // Uploads document:
    private void sendDocUploadingAFile(Long chatId, java.io.File save,String caption) throws TelegramApiException {

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setNewDocument(save);
        sendDocumentRequest.setCaption(caption);
        sendDocument(sendDocumentRequest);
    }

    private void handleFile(String filename, String record) {

        String fileContent = "";
        try {
            fileContent = Fileio.readfile(filename);
            fileContent = fileContent + record;
            Fileio.writefile(filename, fileContent);
        }
        catch(IOException e) {
            e.printStackTrace();
            try {
                Fileio.writefile(filename, record);
            }
            catch(IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String username = update.getMessage().getChat().getUserName();
            String message_text = update.getMessage().getText();
            long userId   = update.getMessage().getChat().getId();
            long chat_id = update.getMessage().getChatId();

            String response = "";

            // Reading associated file:
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat dateFormatDateOnly = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();

            String table_filename_noext = dateFormatDateOnly.format(date);

            // '/getfile' command
            // sending .csv table if requested and if it exists
            if(message_text.equals("/getfile"))
            {
                File file = new File(table_filename_noext + ".csv");
                if(file.exists()) {
                    try {
                        sendDocUploadingAFile(chat_id, file, "Статистика за " + table_filename_noext + ":\n ");
                        return;
                    }
                    catch(TelegramApiException e) { e.printStackTrace(); }
                }
                else {
                    response = "На " + table_filename_noext + " файла не существует";
                    return;
                }
            }


            // make record into csv table:
            String record = userId + ";" + username + ";" + message_text + ";" + dateFormat.format(date) + "\n";
            handleFile(table_filename_noext + ".csv", record);
            response = "Записал";


            // send response to user:
            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(response);
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