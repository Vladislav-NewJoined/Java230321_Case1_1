import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Кейс «Анализатор курса валют».
// 3. Очень сложное:
//- Найти самые сильные скачки в этот промежуток, дни, когда курс сильно вырос или упал. Автоматически скачать текст статьи из википедии, отвечающей за факты на эту дату
// Инфо здесь: Как найти анализ курса валют за определенную дату. Урок 6 Видео мин 0.44.56
// Задание здесь: https://lms.synergy.ru/student/updiscipline/4474947/1045153/1/1

public class Case_3_1_1 {

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
        List<Double> listCourses = new ArrayList<>();

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
                listCourses.add(courseNextDoble);
            } else {
                String courseNextPage = "";
                System.out.println("Курс на " + nextDate);
            }
        }
//Далее ищем максимальные перепады курса.
        double max = maxDifference(listCourses);
        double min = minDifference(listCourses);
        DecimalFormat df = new DecimalFormat("0.000");
        df.setRoundingMode(RoundingMode.DOWN);
        System.out.println("\nЗа указанный месяц курс максимально вырос между двумя соседними датами на величину: " + df.format(max));
        System.out.println("За указанный месяц курс максимально упал между двумя соседними датами на величину: " + df.format(min));
    }

    //Пишем классы для поиска максимальных перепадов курса.
//Сначала максимальную разницу находим.
    public static double maxDifference(List<Double> listCourses) {
        if (listCourses == null || listCourses.size() == 0) {
            return Double.MIN_VALUE;
        }
        int len = listCourses.size();
        double[] diff = new double[len - 1];
        for (int i = 0; i < len - 1; i++) {
            diff[i] = listCourses.get(i + 1) - listCourses.get(i);
        }
        return max(diff);
    }

    public static double max(double[] diff) {
        if (diff == null || diff.length == 0) {
            return Double.MIN_VALUE;
        }
        double max = diff[0];
        for (int i = 0, len = diff.length; i < len; i++) {
            //not necessary,since 'int[] data' is sorted,so 'int[] diff' is progressively increased.
            //int tmp=diff[i]>0?diff[i]:(-diff[i]);
            if (max < diff[i]) {
                max = diff[i];
            }
        }
        return max;
    }

    //Теперь минимальную разницу находим.
    public static double minDifference(List<Double> listCourses) {
        if (listCourses == null || listCourses.size() == 0) {
            return Double.MIN_VALUE;
        }
        int len = listCourses.size();
        double[] diff = new double[len - 1];
        for (int i = 0; i < len - 1; i++) {
            diff[i] = listCourses.get(i + 1) - listCourses.get(i);
        }
        return min(diff);
    }

    public static double min(double[] diff) {
        if (diff == null || diff.length == 0) {
            return Double.MIN_VALUE;
        }
        double min = diff[0];
        for (int i = 0, len = diff.length; i < len; i++) {
            //not necessary,since 'int[] data' is sorted,so 'int[] diff' is progressively increased.
            //int tmp=diff[i]>0?diff[i]:(-diff[i]);
            if (min > diff[i]) {
                min = diff[i];
            }
        }
        return min;
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