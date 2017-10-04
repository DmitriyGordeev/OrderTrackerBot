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

    private DatabaseHandler database;
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private DateFormat dateFormatMonth = new SimpleDateFormat("MM-yyyy");

    public Bot() throws Exception {
        database = new DatabaseHandler();

        String url = "jdbc:mysql://mysql5.gear.host:3306/orderbot";
        String user = "orderbot";
        String pass = "Cc2-_M6KqMWH";

        if(!database.connect(url, user, pass)) {
            throw new Exception("unable to setup database connection");
        }
    }

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


    private String daysumCommand(String request, String dateNoDay, String fulldate) {

        String response = "";
        String[] words = request.split("\\s+");
        if(words.length == 1)
        {
            try {
                response = "Выручка за день: "
                        + Float.toString(getDaySum(dateNoDay + "/" + fulldate + ".csv"));
            }
            catch(IOException e) {
                response = "Файл за " + fulldate + " не найден";
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

        return response;
    }


    private String monthSumCommand(String request, String dateNoDay) {

        String response = "";
        String[] words = request.split("\\s+");
        if(words.length == 1) {
            response = "Выручка за " + dateNoDay + ": \n" + Float.toString(getMonthSum(dateNoDay));
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

        return response;
    }

    /* Updated methods: */
    public float getDaySum_db(String date) {

        float outputValue = 0;
        ArrayList<SaleRecord> saleRecords = database.getRecords(date);
        for(SaleRecord s : saleRecords) {
            outputValue += UpdateParser.parsePrice(s.message);
        }

        return outputValue;
    }
    public String daysumCommand_db(String request) {

        String[] words = request.split("\\s+");
        if(words.length == 1)
        {
            Date today = new Date();
            return Float.toString(getDaySum_db(dateFormat.format(today)));
        }
        else if(words.length == 2) {

            try {
                dateFormat.parse(words[1]);
                return "Выручка за " + words[1] + " : " + Float.toString(getDaySum_db(words[1]));
            }
            catch(ParseException e) {
                return "Неверный формат даты\n" +
                        "Необходимо: dd-MM-yyyy\n" +
                        "Например: /command 01-01-2017";
            }
        }
        return "";
    }

    public float getMonthSum_db(String date) {

        float outputValue = 0;
        ArrayList<SaleRecord> saleRecords = database.getRecordsMonth(date);
        for(SaleRecord s : saleRecords) {
            outputValue += UpdateParser.parsePrice(s.message);
        }

        return outputValue;
    }
    public String monthsumCommand_db(String request) {

        String[] words = request.split("\\s+");
        if(words.length == 1)
        {
            Date today = new Date();
            return Float.toString(getMonthSum_db(dateFormatMonth.format(today)));
        }
        else if(words.length == 2) {

            try {
                dateFormatMonth.parse(words[1]);
                return "Выручка за " + words[1] + " : " + Float.toString(getMonthSum_db(words[1]));
            }
            catch(ParseException e) {
                return "Неверный формат даты\n" +
                        "Необходимо: MM-yyyy\n" +
                        "Например: /command 01-2017";
            }
        }
        return "";
    }

    public void createDayFile(String date) throws IOException {
        ArrayList<SaleRecord> records = database.getRecords(date);
        String fileContent = "";
        for(SaleRecord s : records) {
            fileContent += s.csv("dd-MM-yyyy") + "\n";
        }

        Fileio.writefile("dayfile.csv", fileContent);
    }
    public String prepareForUpload(Date date, long chat_id) {

        String response = "";
        try { createDayFile(dateFormat.format(date)); }
        catch(IOException e) {
            response = "Не удалось создать файл выгрузки\n";
            return response;
        }

        File file = new File("dayfile.csv");
        try {
            sendDocUploadingAFile(chat_id, file, "Отчет за " + dateFormat.format(date));
        }
        catch(TelegramApiException e) {
            response = "Не удалось выгрузить файл";
            return response;
        }

        return response;
    }
    public String dayFileCommand_db(String request, long chat_id) {

        String response = "";
        String[] words = request.split("\\s+");
        if(words.length == 1) {
            Date today = new Date();
            response = prepareForUpload(today, chat_id);
        }
        else if(words.length == 2)
        {
            try {
                Date date = dateFormat.parse(words[1]);
                response = prepareForUpload(date, chat_id);
            }
            catch(ParseException e) {
                return "Неверный формат даты\n" +
                        "Необходимо: dd-MM-yyyy\n" +
                        "Например: /command 01-01-2017";
            }
        }

        return response;
    }

    public void createMonthFile(String date) throws IOException {
        ArrayList<SaleRecord> records = database.getRecordsMonth(date);
        String fileContent = "";
        for(SaleRecord s : records) {
            fileContent += s.csv("dd-MM-yyyy") + "\n";
        }

        Fileio.writefile("monthfile.csv", fileContent);
    }
    public String prepareMonthForUpload(Date date, long chat_id) {

        String response = "";
        try { createMonthFile(dateFormatMonth.format(date)); }
        catch(IOException e) {
            response = "Не удалось создать файл выгрузки\n";
            return response;
        }

        File file = new File("monthfile.csv");
        try {
            sendDocUploadingAFile(chat_id, file, "Отчет за " + dateFormatMonth.format(date));
        }
        catch(TelegramApiException e) {
            response = "Не удалось выгрузить файл";
            return response;
        }

        return response;
    }
    public String monthFileCommand_db(String request, long chat_id) {

        String response = "";
        String[] words = request.split("\\s+");
        if(words.length == 1) {
            Date today = new Date();
            response = prepareMonthForUpload(today, chat_id);
        }
        else if(words.length == 2)
        {
            try {
                Date date = dateFormatMonth.parse(words[1]);
                response = prepareMonthForUpload(date, chat_id);
            }
            catch(ParseException e) {
                return "Неверный формат даты\n" +
                        "Необходимо: MM-yyyy\n" +
                        "Например: /command 01-01-2017";
            }
        }

        return response;
    }

    /* -------------------------------------------------------------------- */

    private SendMessage setupKeyboard(long chat_id) {

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
        sendMessage.setChatId(chat_id);
//        sendMessage.setReplyToMessageId(message.getMessageId());

        return sendMessage;
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

            SendMessage sendMessage = setupKeyboard(chat_id);
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
                        "Просто пишите сюда информацию о продаже, например 'Название услуги 250'";
            }
            else if(request.equals("/help")) {
                response = "Доступны следующие команды:\n\n" +
                        "/daysum - выручка за сегодня\n\n" +
                        "/daysum 01-09-2017 - выручка за указаную дату\n\n" +
                        "/getfile - файл-отчет по сегодняшним продажам\n\n" +
                        "/getfile 01-09-2017 - файл-отчет за указаную дату, например 1 сентября\n\n" +
                        "/monthsum - выручка за текущий месяц\n\n" +
                        "/monthsum 09-2017 - выручка за указаный месяц\n\n" +
                        "/monthfile - файл-отчет по текущему месяцу\n\n" +
                        "/monthfile 09-2017 - файл-отчет за указаный месяц\n\n";
            }
            else if(request.equals("/settings")) {

            }
            else if(request.contains("/monthfile")) {
                // response = monthFileCommand(request, folderName, chat_id);
                response = monthFileCommand_db(request, chat_id);
            }
            else if(request.contains("/daysum")) {
                // response = daysumCommand(request, folderName, table_filename_noext);
                response = daysumCommand_db(request);
            }
            else if(request.contains("/monthsum")) {
                // response = monthSumCommand(request, folderName);
                response = monthsumCommand_db(request);
            }
            else if(request.contains("/getfile")) {
                // response = dayFileCommand(request, date, chat_id);
                response = dayFileCommand_db(request, chat_id);
            }
            else {

//                // check if such date-folder exists:
//                File folder = new File(folderName);
//                if(!folder.exists() || !folder.isDirectory())
//                    folder.mkdir();
//
//                // make record into csv table:
//                String record = userId + ";" + username + ";" + request + ";" + dateFormat.format(date) + "\n";
//                makeRecord(folderName + "/" + table_filename_noext + ".csv", record);
//                response = "Записал";


                SaleRecord userInput = new SaleRecord();
                userInput.userId = userId;
                userInput.username = username;
                userInput.message = request;
                userInput.date = date;

                if(database.insertRecord(userInput))
                    response = "Записал";
                else
                    response = "Запись не удалась, возможно База данных отключена";
            }


            sendMessage.setText(response);
            try {
                sendMessage(sendMessage);
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


    /* transforms dateString into newFormat style: */
    public String reformateDate(String dateString, String inputFormat, String newformat) throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat(inputFormat);
        Date date = dateFormat.parse(dateString);

        dateFormat = new SimpleDateFormat(newformat);
        return dateFormat.format(date);
    }


}