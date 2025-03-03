package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Класс, представляющий запрос на предмет.
 *
 * <p>Этот класс используется для хранения информации о запросе на предмет,
 * включая уникальный идентификатор, описание, пользователя, создавшего запрос,
 * и дату создания запроса.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    /**
     * Уникальный идентификатор запроса.
     *
     * @return уникальный идентификатор запроса
     */
    private Long id;

    /**
     * Текст запроса, содержащий описание требуемой вещи.
     *
     * @return описание запрашиваемой вещи
     */
    private String description;

    /**
     * Пользователь, создавший запрос.
     *
     * @return пользователь, создавший запрос
     */
    private User requestor;

    /**
     * Дата и время создания запроса.
     *
     * @return дата и время создания запроса
     */
    private LocalDateTime created;
}