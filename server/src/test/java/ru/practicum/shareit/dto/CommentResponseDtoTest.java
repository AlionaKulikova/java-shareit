package ru.practicum.shareit.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.annotation.Rollback;
import ru.practicum.shareit.comment.CommentResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentResponseDtoTest {

    @Autowired
    private JacksonTester<CommentResponseDto> jacksonTester;

    @Test
    @SneakyThrows
    @Rollback(true)
    void testCommentResponseDto() {
        CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Отлично")
                .authorName("Иван")
                .created(LocalDateTime.of(2023, 7, 7, 12, 12))
                .build();

        JsonContent<CommentResponseDto> commentResponseDtoJsonContent = jacksonTester.write(commentResponseDto);

        assertThat(commentResponseDtoJsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(commentResponseDtoJsonContent).extractingJsonPathStringValue("$.text").isEqualTo("Отлично");
        assertThat(commentResponseDtoJsonContent).extractingJsonPathStringValue("$.authorName").isEqualTo("Иван");
    }

    @Test
    @SneakyThrows
    @Rollback(true)
    void testCommentDtoRead() {
        String comment
                = "{\"id\":1,\"text\":\"Отлично\",\"authorName\":\"Иван\",\"created\":\"2023-07-07T12:12\"}";
        CommentResponseDto commentResponseDto = jacksonTester.parseObject(comment);

        assertThat(1L).isEqualTo(commentResponseDto.getId());
        assertThat("Отлично").isEqualTo(commentResponseDto.getText());
        assertThat("Иван").isEqualTo(commentResponseDto.getAuthorName());
        assertThat("2023-07-07T12:12").isEqualTo(commentResponseDto.getCreated().toString());
    }
}
