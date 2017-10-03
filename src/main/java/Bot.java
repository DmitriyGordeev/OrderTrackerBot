import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Bot extends TelegramLongPollingBot {

    // Uploads document:
    private void sendDocUploadingAFile(Long chatId, java.io.File save,String caption) throws TelegramApiException {

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setNewDocument(save);
        sendDocumentRequest.setCaption(caption);
        sendDocument(sendDocumentRequest);
    }


    private void makeRecord(String filename, String record) {

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


    public float getDaySum(String filename) throws IOException {

        float day_total = 0;
        String content = Fileio.readfile(filename);
        String[] rows = content.split("\n");

        for(String s : rows)
        {
            // TODO: change column-take method:
            String[] cols = s.split(";");

            // TODO: remove hardcode 'cols.length == 4'
            // TODO: and cols[2]
            if(cols.length == 4) {
                float value = UpdateParser.parsePrice(cols[2]);
                if(value != -1) {
                    day_total += value;
                }
            }
        }

        return day_total;
    }


    public float getMonthSum(String monthFolderName) {

        float month_total = 0;
        File folder = new File(monthFolderName);

        if(!folder.exists()) {
            return 0;
        }

        String[] ext = {"csv"};
        List<File> files = (List<File>) FileUtils.listFiles(folder, ext, true);

        for(File f : files) {

            try {
                month_total += getDaySum(f.getPath());
            }
            catch(IOException e) {}
        }

        return month_total;
    }


    private String getDayFileForUpload(String dateValue, long chat_id) {

        String response = "";
        File file = new File(dateValue + ".csv");
        if(file.exists()) {
            try {
                sendDocUploadingAFile(chat_id, file, "Статистика за " + dateValue + "\n ");
            }
            catch(TelegramApiException e) { e.printStackTrace(); }
        }
        else {
            response = "На " + dateValue + " файла не существует";
        }

        return response;
    }


    private String getMonthFileForUpload(String monthFolder, long chat_id) {

        String response = "";
        File file = new File(monthFolder);
        if(!file.exists()) {
            return "Записи на " + monthFolder + " не найдены";
        }

        String[] ext = {"csv"};
        List<File> files = (List<File>) FileUtils.listFiles(file, ext, true);

        // concantenate all .csv files into one
        String totalContentOfTables = "";
        for(File f : files) {
            try {
                String content = Fileio.readfile(f.getPath());
                totalContentOfTables += content;
            } catch (IOException e) { }
        }

        // write total content into new file:
        try {
            Fileio.writefile(monthFolder + "/" + monthFolder + ".csv", totalContentOfTables);
            try {
                File f = new File(monthFolder + "/" + monthFolder + ".csv");
                sendDocUploadingAFile(chat_id, f, "Статистика за " + monthFolder + "\n ");
            }
            catch(TelegramApiException e) {
                response = "Не удалось отправить файл-отчет";
            }
        }
        catch(IOException e) {
            response = "Не удалось создать файл-отчет";
        }

        return response;
    }


    private String monthFileCommand(String request, String dateNoDay, long chat_id) {

        String response = "";
        String[] words = request.split("\\s+");
        if(words.length == 1) {
            response = getMonthFileForUpload(dateNoDay, chat_id);
        }
        else if(words.length == 2)
        {
            // custom month
            // example '/monthfile 10-2017'
            try {
                        /* TODO: refactor inputFormat here! */
                String custom_month_folder = reformateDate(words[1], "MM-yyyy", "MM-yyyy");
                response = getMonthFileForUpload(custom_month_folder, chat_id);
            }
            catch(ParseException e) {
                response =
                        "Неправильный формат даты\n" +
                                "Необходимо: dd-MM-yyyy\n" +
                                "Например: /monthfile 01-2017";
            }
        }

        return response;

    }


    private String dayFileCommand(String request, Date date, long chat_id) {

        SimpleDateFormat fullDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateNoDay = new SimpleDateFormat("MM-yyyy");

        String response = "";
        String[] words = request.split("\\s+");
        if(words.length == 1) {
            response = getDayFileForUpload(dateNoDay.format(date) + "/" + fullDate.format(date), chat_id);
        }
        else if(words.length == 2)
        {
            // custom date
            // example '/getfile 02-10-2017'

            if(words[1].equals("вчера")) {
                Date d = new Date(date.getTime() - 24 * 3600 * 1000);
                String filename = fullDate.format(d);
                response = getDayFileForUpload(filename, chat_id);
            }
            else {
                SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    Date d = parser.parse(words[1]);
                    String custom_date_folder = reformateDate(words[1],
                            "dd-MM-yyyy",
                            "MM-yyyy");

                    response = getDayFileForUpload( custom_date_folder + "/" + words[1], chat_id);
                }
                catch(ParseException e) {
                    e.printStackTrace();
                    response =
                            "Неправильный формат даты\n" +
                                    "Необходимо: dd-MM-yyyy\n" +
                                    "Например: /getfile 01-01-2017";
                }
            }
        }

        return response;
    }


    public void sendMessageWithKeyboard(Message message, String text) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        ArrayList<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Файл-отчет за сегодня");

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add("Выручка за сегодня");

        // Третья строчка клавиатуры
        KeyboardRow keyboardThirdRow = new KeyboardRow();
        keyboardThirdRow.add("Выручка за этот месяц");

        KeyboardRow keyboardFourthRow = new KeyboardRow();
        keyboardFourthRow.add("Файл-отчет за этот месяц");

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThirdRow);
        keyboard.add(keyboardFourthRow);

        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());


        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    // transforms human-readable button caption into /command
    public String messageCommand(String message) {

        if(message.equals("Файл-отчет за сегодня")) {
            return "/getfile";
        }
        else if(message.equals("Выручка за сегодня")) {
            return "/daysum";
        }
        else if(message.equals("Выручка за этот месяц")) {
            return "/monthsum";
        }
        else if(message.equals("Файл-отчет за этот месяц")) {
            return "/monthfile";
        }

        return message;
    }


    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String username = update.getMessage().getChat().getUserName();
            String request = update.getMessage().getText();
            long userId   = update.getMessage().getChat().getId();
            long chat_id = update.getMessage().getChatId();

            request = messageCommand(request);

            String response = "";

            // Reading associated file:
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            DateFormat dateFormatDateOnly = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat dateFormatNoDay = new SimpleDateFormat("MM-yyyy");

            Date date = new Date();

            String table_filename_noext = dateFormatDateOnly.format(date);
            String folderName = dateFormatNoDay.format(date);

            // TODO: refactor to switch - case:
            if(request.equals("/start")) {
                response =
                        "Приветствую!\n" +
                        "Я собираю информацию по заказам на текущий день.\n" +
                        "Просто пишите сюда сумму продажи, например '250'";
            }
            else if(request.equals("/help")) {

            }
            else if(request.equals("/settings")) {

            }
            else if(request.contains("/monthfile")) {
                response = monthFileCommand(request, folderName, chat_id);
            }
            else if(request.contains("/daysum")) {

                String[] words = request.split("\\s+");
                if(words.length == 1)
                {
                    try {
                        response = "Выручка за день: "
                                + Float.toString(getDaySum(folderName + "/" + table_filename_noext + ".csv"));
                    }
                    catch(IOException e) {
                        response = "Файл за " + table_filename_noext + " не найден";
                    }
                }
                else if(words.length == 2)
                {
                    SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
                    try {
                        Date d = parser.parse(words[1]);
                        String custom_date_folder = reformateDate(words[1],
                                "dd-MM-yyyy",
                                "MM-yyyy");

                        try {
                            response = "Выручка за " + words[1] + ":\n" +
                                    Float.toString(getDaySum( custom_date_folder + "/" + words[1] + ".csv"));
                        }
                        catch(IOException e) {
                            response = "Файл за " + words[1] + " не найден";
                        }
                    }
                    catch(ParseException e) {
                        e.printStackTrace();
                        response =
                                "Неправильный формат даты\n" +
                                        "Необходимо: dd-MM-yyyy\n" +
                                        "Например: /daysum 01-01-2017";
                    }
                }
            }
            else if(request.contains("/monthsum")) {

                String[] words = request.split("\\s+");
                if(words.length == 1) {
                    response = "Выручка за " + folderName + ": \n" + Float.toString(getMonthSum(folderName));
                }
                else if(words.length == 2)
                {
                    SimpleDateFormat parser = new SimpleDateFormat("MM-yyyy");
                    try {
                        Date d = parser.parse(words[1]);
                        response = "Выручка за " + words[1] + ": \n" + Float.toString(getMonthSum(words[1]));
                    }
                    catch(ParseException e) {
                        e.printStackTrace();
                        response =
                                "Неправильный формат даты\n" +
                                        "Необходимо: dd-MM-yyyy\n" +
                                        "Например: '/monthsum 01-2017' - выручка за январь 2017";
                    }
                }
            }
            else if(request.contains("/getfile")) {
                response = dayFileCommand(request, date, chat_id);
            }
            else {

                // check if such date-folder exists:
                File folder = new File(folderName);
                if(!folder.exists() || !folder.isDirectory())
                    folder.mkdir();

                // make record into csv table:
                String record = userId + ";" + username + ";" + request + ";" + dateFormat.format(date) + "\n";
                makeRecord(folderName + "/" + table_filename_noext + ".csv", record);
                response = "Записал";
            }


            sendMessageWithKeyboard(update.getMessage(), response);
        }

    }


    public String getBotUsername() {
        return "OrderTrackerBot_bot";
    }


    public String getBotToken() {
        return "445107190:AAGSZJHeTLrzcq2AAFGVuMn20C1xEFU6A5U";
    }


    /* transforms dateString into newFormat style: */
    public String reformateDate(String dateString, String inputFormat, String newformat) throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat(inputFormat);
        Date date = dateFormat.parse(dateString);

        dateFormat = new SimpleDateFormat(newformat);
        return dateFormat.format(date);
    }


}