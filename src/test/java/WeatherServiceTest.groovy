import static org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WeatherServiceTest {

    private WeatherService service

    @Test
    void setup() {
        service = new WeatherService()
    }

    @Test
    void testBuildUrl_withValidParams() {
        // Вызываем метод из WeatherService
        String url = service.buildUrl(55.75, 37.62, "ru_RU", 5, false, false)

        // Проверяем, что URL собран корректно
        assertEquals(
                "https://api.weather.yandex.ru/v2/forecast?lat=55.75&lon=37.62&lang=ru_RU&limit=5&hours=false&extra=false",
                url
        )
    }

    @Test
    void testGetCurrentTemperature_fromValidJson() {
        // Пример JSON-ответа от API
        String json = '''
        {
            "fact": {
                "temp": 22
            }
        }
        '''

        int temperature = service.getCurrentTemperature(json)

        assertEquals(22, temperature)
    }

    @Test
    void testGetCurrentTemperature_whenTempIsNull() {
        String json = '''
        {
            "fact": {}
        }
        '''

        int temperature = service.getCurrentTemperature(json)

        // Предполагаем, что метод возвращает 0 при отсутствии temp
        assertEquals(0, temperature)
    }
}