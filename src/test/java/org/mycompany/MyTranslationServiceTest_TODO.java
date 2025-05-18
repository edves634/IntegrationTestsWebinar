package org.mycompany;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MyTranslationServiceTest {

    private Translate googleTranslateMock; // Мок-объект для интерфейса Google Translate
    private MyTranslationService translationService; // Тестируемый сервис перевода

    @BeforeEach
    void setUp() {
        // Инициализация мок-объекта перед каждым тестом
        googleTranslateMock = Mockito.mock(Translate.class);
        // Создание экземпляра сервиса перевода с использованием мок-объекта
        translationService = new MyTranslationService(googleTranslateMock);
    }

    /**
     * 1. Тест для успешного перевода.
     * Проверяет, что метод translateWithGoogle корректно переводит предложение на русский язык.
     */
    @Test
    void translateWithGoogle_anySentenceAndTargetLanguageIsRu_success() {
        // Подготовка данных для теста
        String sentence = "Hello"; // Исходное предложение на английском
        String targetLanguage = "ru"; // Целевой язык - русский
        String translatedText = "Привет"; // Ожидаемый перевод

        // Создание мок-объекта для перевода
        Translation translationMock = Mockito.mock(Translation.class);
        // Настройка поведения мок-объекта Google Translate
        when(googleTranslateMock.translate(anyString(), any())).thenReturn(translationMock);
        when(translationMock.getTranslatedText()).thenReturn(translatedText); // Настройка возвращаемого текста перевода

        // Вызов метода для перевода
        String result = translationService.translateWithGoogle(sentence, targetLanguage);

        // Проверка результата перевода
        assertEquals(translatedText, result); // Проверка, что результат соответствует ожидаемому переводу
        verify(googleTranslateMock, times(1)).translate(sentence, Translate.TranslateOption.targetLanguage(targetLanguage)); // Проверка, что метод translate был вызван один раз с правильными параметрами
        verifyNoMoreInteractions(googleTranslateMock); // Проверка, что больше никаких взаимодействий с мок-объектом не было
    }

    /**
     * 2. Тест для случая, когда целевой язык не поддерживается.
     * Проверяет, что метод выбрасывает исключение IllegalArgumentException,
     * если целевой язык не является русским.
     */
    @Test
    void translateWithGoogle_anySentenceAndTargetLanguageIsNotRu_failure() {
        // Подготовка данных для теста
        String sentence = "Hello"; // Исходное предложение на английском
        String targetLanguage = "en"; // Целевой язык - английский (недопустимый)

        // Действие и проверка результата
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            translationService.translateWithGoogle(sentence, targetLanguage); // Вызов метода с недопустимым языком
        });

        // Проверка сообщения исключения
        assertEquals("only translation to Russian is currently supported!", thrown.getMessage());
        verifyNoInteractions(googleTranslateMock); // Проверка, что метод Google Translate не был вызван
    }

    /***
     * 3. Тест для случая, когда вызов Google Translate вызывает исключение.
     * Проверяет, что метод выбрасывает MyTranslationServiceException,
     * если при вызове API Google Translate возникает ошибка.
     */
    @Test
    void translateWithGoogle_googleTranslateThrowsException_failure() {
        // Подготовка данных для теста
        String sentence = "Hello"; // Исходное предложение на английском
        String targetLanguage = "ru"; // Целевой язык - русский

        // Настройка поведения мок-объекта для выброса исключения при вызове метода translate
        when(googleTranslateMock.translate(anyString(), any())).thenThrow(new RuntimeException("API call failed"));

        // Действие и проверка результата
        MyTranslationServiceException thrown = assertThrows(MyTranslationServiceException.class, () -> {
            translationService.translateWithGoogle(sentence, targetLanguage); // Вызов метода, который должен выбросить исключение
        });

        // Проверка сообщения исключения и его причины
        assertEquals("Exception while calling Google Translate API", thrown.getMessage());
        assertTrue(thrown.getCause() instanceof RuntimeException); // Проверка, что причиной является RuntimeException
        verify(googleTranslateMock, times(1)).translate(sentence, Translate.TranslateOption.targetLanguage(targetLanguage)); // Проверка, что метод translate был вызван один раз с правильными параметрами
    }
}
