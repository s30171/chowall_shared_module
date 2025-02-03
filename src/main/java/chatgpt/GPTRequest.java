package chatgpt;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GPTRequest {

    private String model;
    private List<Message> messages;
    private int n;
    private double temperature = 0;
    @JsonProperty("response_format")
    private Map<String, Object> responseFormat;

    public GPTRequest(String model, String prompt, int n) {
        this.model = model;
        this.n = n;
        this.messages = new ArrayList<>();
        this.messages.add(new Message(MessageRole.USER, prompt));
    }

    public GPTRequest(String model, String systemPrompt, String userPrompt, int n, double temperature) {
        this.model = model;
        this.n = n;
        this.messages = new ArrayList<>();
        if (StringUtils.isNotBlank(systemPrompt)) {
            this.messages.add(new Message(MessageRole.SYSTEM, systemPrompt));
        }
        this.messages.add(new Message(MessageRole.USER, userPrompt));
        if (temperature > 0) {
            this.temperature = temperature;
        }
    }

    public GPTRequest(String model, String systemPrompt, String userPrompt, int n, double temperature, Map<String, Object> responseFormat) {
        this.model = model;
        this.n = n;
        this.messages = new ArrayList<>();
        if (StringUtils.isNotBlank(systemPrompt)) {
            this.messages.add(new Message(MessageRole.SYSTEM, systemPrompt));
        }
        this.messages.add(new Message(MessageRole.USER, userPrompt));
        if (temperature > 0) {
            this.temperature = temperature;
        }
        if (responseFormat != null) {
            this.responseFormat = responseFormat;
        }
    }

    // 使用 @JsonGetter 並搭配 @JsonInclude 當回傳 null 時不序列化該 key
    @JsonGetter("temperature")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getTemperature() {
        // 如果模型是 o1 或 o3，則不傳送 temperature
        if (model.contains("o1") || model.contains("o3")) {
            return null;
        }
        return temperature;
    }
}
