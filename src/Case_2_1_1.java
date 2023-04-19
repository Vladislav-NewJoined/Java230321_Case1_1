import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Case_2_1_1 {
    // Кейс «Анализатор курса валют».
    // Задание 2. - Пользователь вводит месяц и год. Вывести курс рубля за этот месяц, найти наибольший и наименьшие значения
    // Инфо здесь: Как найти анализ курса валют за определенную дату. Урок 6 Видео мин 0.44.56
    // https://lms.synergy.ru/student/updiscipline/4474947/1045153/1/1
    // Тоже: Как найти минимум и максимум массива | Фрагмент лекции JavaRush https://youtu.be/DO1xhnyu5cw
    public static void main(String[] args) throws IOException, ParseException {
        BufferedReader buffered = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Введите исходные месяц и год с разделителем '/', пример: 02/2020:");
        String origMonth = buffered.readLine();  // Start month

        // Делаем парсинг введённой строки методом Split.
        String[] items = origMonth.split("/");
        String mon = items[0];
        String yea = items[1];

        int monI = Integer.parseInt(mon);
        int yeaI = Integer.parseInt(yea);

        // Преобразовываем ввод через переменную YearMonth.
        YearMonth ym = YearMonth.of(yeaI, monI);

        // Скачиваем исходный код веб-страницы Центробанка.
        String originalPage = downloadWebPage("https://cbr.ru/scripts/XML_dynamic.asp?date_req1=12/11/2021&date_req2=12/11/2021&VAL_NM_RQ=R01235");
        // Задаём адрес исходной веб-страницы Центробанка в текстовом формате.
        String originalPageText = "https://cbr.ru/scripts/XML_dynamic.asp?date_req1=12/11/2021&date_req2=12/11/2021&VAL_NM_RQ=R01235";

//    get the last day of month
        int lastDay = ym.lengthOfMonth();
//    Создаем массив ArrayList, куда записываем в качестве элементов курс на текущую дату.
        List<Double> list = new ArrayList<>();

//    loop through the days
        for (int day = 1; day <= lastDay; day++) {
            // create the day
            LocalDate dt = ym.atDay(day);
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dtStr = dt.format(f);
            // set to midnight at JVM default timezone
            int startIndex = originalPage.lastIndexOf("<Value>") + 7;
            int endIndex = originalPage.lastIndexOf("</Value>");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(String.valueOf(dtStr)));
            String nextDate;
            nextDate = sdf.format(c.getTime());  // entering nextDate

            // Меняем в адресе исходной страницы дату на следующую.
            String urlWithNextDate = originalPageText.replaceAll("12/11/2021", nextDate);

            String nextPage = downloadWebPage(urlWithNextDate);

            if (nextPage.contains("<Value>")) {
                String courseNextPage = nextPage.substring(startIndex, endIndex);
                // Задаём курс в виде переменной Double.
                double courseNextDoble = Double.parseDouble(courseNextPage.replace(",", "."));
                // System.out.println("Курс в типе переменной Double:");
                // System.out.println(courseNextDoble);
                // Выводим на экран дату и соответствующий курс.
                System.out.println("Курс на " + nextDate + "    " + courseNextDoble);
                list.add(courseNextDoble);
            } else {
                String courseNextPage = "";
                System.out.println("Курс на " + nextDate);
            }
        }
//        System.out.println(list);
//        System.out.println(list.get(1));

//        Нахождение наибольшего и наименьшего значений.
//        Инициализируем исходную переменную: от которой начинаем считать.
        Double min = list.get(0);
        Double max = list.get(0);
//        Инициализируем переменную: длина (или размер) массива.
        int n = list.size();

//        Задаем цикл перебора массива для поиска наибольшего и наименьшего значений.
        for (int i = 1; i < n; i++) {
            if (list.get(i) > max) {
                max = list.get(i);
            }
        }

        for (int i = 1; i < n; i++) {
            if (list.get(i) < min) {
                min = list.get(i);
            }
        }
        System.out.println("\nНаибольшее значение курса: " + max);
        System.out.println("Наименьшее значение курса: " + min);
    }

    private static String downloadWebPage(String url) throws IOException {
        StringBuilder result = new StringBuilder();
        String line;
        URLConnection urlConnection = new URL(url).openConnection();
        try (InputStream is = urlConnection.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }
}