package io.github.flaviodotcom.notification.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Problem {

    private final int status;
    private final OffsetDateTime timestamp;
    private final String title;
    private final String detail;
    private final List<ProblemObject> messages;

    public Problem(int status, String title, String detail) {
        this.status = status;
        this.timestamp = OffsetDateTime.now();
        this.title = title;
        this.detail = detail;
        this.messages = new ArrayList<>();
    }

    public void addMessage(String name, String message) {
        this.messages.add(new ProblemObject(name, message));
    }
}
