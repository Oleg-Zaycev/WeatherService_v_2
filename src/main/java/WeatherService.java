import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherService {

    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final String API_KEY = "74147f55-ce05-44aa-b184-706f4ce783ab";

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private final HttpClient client;
    private final ObjectMapper mapper;

    public WeatherService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    // Метод для формирования URL
    public String buildUrl(double lat, double lon, String lang, int limit, boolean hours, boolean extra) {
        return String.format("%s?lat=%s&lon=%s&lang=%s&limit=%s&hours=%s&extra=%s",
                API_URL, lat, lon, lang, limit, hours, extra);
    }

    // Метод для отправки запроса и получения JSON-ответа
    public String fetchWeatherData(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            throw new IllegalArgumentException("Ошибка 404: Некорректные координаты (lat/lon)");
        } else if (response.statusCode() != 200) {
            throw new IOException("Ошибка HTTP: " + response.statusCode() + " — " + response.body());
        }

        return response.body();
    }

    // Метод для извлечения текущей температуры из JSON
    public int getCurrentTemperature(String jsonString) throws Exception {
        JsonNode root = mapper.readTree(jsonString);
        JsonNode fact = root.path("fact");

        if (fact.isMissingNode()) {
            throw new Exception("Ошибка: Текущая температура не указана.");
        }

        return fact.path("temp").asInt();
    }

    // Метод для расчёта средней температуры по прогнозу
    public double getAverageForecastTemperature(String jsonString) throws Exception {
        JsonNode root = mapper.readTree(jsonString);
        JsonNode forecasts = root.path("forecasts");

        if (forecasts.isMissingNode() || forecasts.isEmpty()) {
            throw new Exception("Ошибка: Нет данных прогноза");
        }

        double sumAvgTemp = 0;
        int count = 0;

        for (int i = 0; i < forecasts.size(); i++) {
            JsonNode dayForecast = forecasts.get(i);
            JsonNode parts = dayForecast.path("parts");
            JsonNode day = parts.path("day");

            if (day.has("temp_avg")) {
                double tempAvg = day.path("temp_avg").asDouble();
                sumAvgTemp += tempAvg;
                count++;
            }
        }

        return count > 0 ? sumAvgTemp / count : 0;
    }

    // Основной метод (теперь просто вызывает логику)
    public static void main(String[] args) {
        WeatherService service = new WeatherService();

        double lat = 55.75;
        double lon = 37.62;
        String lang = "ru_RU";
        int limit = 5;
        boolean hours = false;
        boolean extra = false;

        try {
            String url = service.buildUrl(lat, lon, lang, limit, hours, extra);
            String jsonString = service.fetchWeatherData(url);

            System.out.println("=== Полный ответ от API ===");
            System.out.println(jsonString);

            int currentTemp = service.getCurrentTemperature(jsonString);
            System.out.println("\n=== Текущая температура ===");
            System.out.println("Температура сейчас: " + currentTemp + " °C");

            double avgTemp = service.getAverageForecastTemperature(jsonString);
            System.out.println("\n=== Средняя температура по прогнозу ===");
            System.out.printf("Средняя температура за %d суток: %.1f °C%n", limit, avgTemp);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void doSomething() {
        log.info("Метод вызван");
    }
}