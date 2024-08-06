package ru.api.aggeditor.calculator.validator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;

/**
 * Класс для работы с API Честного знака
 */
public class CrptApi {

    private final static String CREATE_DOCUMENT_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    //Количество запросов
    private final int requestLimit;

    //Временной интервал
    private final TimeUnit timeUnit;

    private final Semaphore semaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new RuntimeException("Не верно задано ограничение на количество запросов");
        }

        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;

        int permits = (int) (requestLimit / timeUnit.toMillis(1000L));
        semaphore = new Semaphore(permits);
    }

    /**
     * Создание документа для ввода в оборот товара, произведенного в РФ. Документ и подпись должны передаваться в метод
     * в виде Java объекта и строки соответственно.
     *
     * @param dto  - Документ
     * @param sign - Подпись
     */
    public void createDocumentRequest(LkDocumentDto dto, String sign) {
        URL url;
        HttpsURLConnection conn = null;
        try {
            url = new URL(CREATE_DOCUMENT_URL);
        } catch (MalformedURLException e) {
            return;
        }
        try {
            semaphore.tryAcquire();
            conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            ObjectMapper mapper = new ObjectMapper();

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.writeBytes(mapper.writeValueAsString(dto));
            }
        } catch (IOException e) {
            ;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            semaphore.release();
        }
    }

    public int getRequestLimit() {
        return requestLimit;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    private static class LkDocumentDto {

        @JsonProperty(value = "description")
        private Description description;

        @JsonProperty(value = "doc_id")
        private String docId;

        @JsonProperty(value = "doc_status")
        private String docStatus;

        //Можно сделать через enum, но не стал, так как не известно сколько типов,
        // а ради 1 нет смысла
        @JsonProperty(value = "doc_type")
        private String docType;

        @JsonProperty(value = "importRequest")
        private boolean importRequest;

        @JsonProperty(value = "owner_inn")
        private String ownerInn;

        @JsonProperty(value = "participant_inn")
        private String participantInn;

        @JsonProperty(value = "producer_inn")
        private String producerInn;

        @JsonFormat(pattern = "YYYY-MM-dd")
        @JsonProperty(value = "production_date")
        private String productionDate;

        @JsonProperty(value = "production_type")
        private String productionType;

        @JsonProperty(value = "products")
        private List<Product> products;

        @JsonFormat(pattern = "YYYY-MM-dd")
        @JsonProperty(value = "reg_date")
        private String regDate;

        @JsonProperty(value = "reg_number")
        private String regNumber;

        //setters getters
    }

    private static class Description {
        private String participantInn;

        //setters getters
    }

    private static class Product {
        @JsonProperty(value = "certificate_document")
        private String certificateDocument;

        @JsonFormat(pattern = "YYYY-MM-dd")
        @JsonProperty(value = "certificate_document_date")
        private String certificateDocumentDate;

        @JsonProperty(value = "certificate_document_number")
        private String certificateDocumentNumber;

        @JsonProperty(value = "owner_inn")
        private String ownerInn;

        @JsonProperty(value = "producer_inn")
        private String producerInn;

        @JsonFormat(pattern = "YYYY-MM-dd")
        @JsonProperty(value = "production_date")
        private String productionDate;

        @JsonProperty(value = "tnved_code")
        private String tnvedCode;

        @JsonProperty(value = "uit_code")
        private String uitCode;

        @JsonProperty(value = "uitu_code")
        private String uituCode;

        //setters getters
    }


}
