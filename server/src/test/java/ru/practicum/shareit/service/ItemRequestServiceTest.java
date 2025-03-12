package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceManager;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {
    private User mockUser1;
    private User mockUser2;
    private Item mockItem1;
    private Item mockItem2;
    private ItemRequest mockItemRequest1;
    private ItemRequest mockItemRequest2;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    ItemRequestServiceManager itemRequestServiceImpl;

    private MockitoSession session;

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        itemRequestServiceImpl = new ItemRequestServiceManager(itemRequestRepository, itemRepository, userRepository);
        mockUser1 = new User(1L, "Иван", "ivan@yandex.ru");
        mockUser2 = new User(2L, "Петр", "petr@yandex.ru");
        mockItem1 = new Item(1L, "Книга", "Описание книги", true, mockUser1, 1L);
        mockItem2 = new Item(2L, "Телефон", "Описание телефона", true, mockUser2, 2L);
        mockItemRequest1 = new ItemRequest(1L, "Требуется книга", mockUser2,
                LocalDateTime.of(2021, 12, 12, 1, 1, 1));
        mockItemRequest2 = new ItemRequest(2L, "Требуется телефон", mockUser1,
                LocalDateTime.of(2021, 12, 12, 1, 1, 1));
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    public void createTest() {
        ItemRequest itemRequest = mockItemRequest1;
        ItemRequestDto itemRequestDto = ItemRequestMapper.itemRequestToDto(mockItemRequest1);
        Mockito
                .when(itemRequestRepository.save(any()))
                .thenReturn(mockItemRequest1);
        Mockito
                .when(userRepository.findById(any()))
                .thenReturn(Optional.of(mockUser1));
        ItemRequestDto itemRequestDto2 = itemRequestServiceImpl.createRequest(itemRequestDto, mockUser1.getId());
        Mockito.verify(itemRepository, never()).save(mockItem1);
        Assertions.assertNotNull(itemRequestDto2);
        Assertions.assertEquals(itemRequestDto.getId(), itemRequestDto2.getId());
        Assertions.assertEquals(itemRequestDto.getDescription(), itemRequestDto2.getDescription());
    }

    @Test
    public void createShouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        ItemRequestDto itemRequestDto = ItemRequestMapper.itemRequestToDto(mockItemRequest1);
        Long userId = 1L;
        Mockito
                .when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestServiceImpl.createRequest(itemRequestDto, userId));
        Mockito.verify(itemRequestRepository, never()).save(any());
    }

    @Test
    public void testGetAllForRequestorEmptyList() {
        User user = mockUser1;
        Mockito
                .when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findAllByRequestor_idOrderByCreatedAsc(user.getId()))
                .thenReturn(List.of());
        List<ItemRequestResponseDto> result = itemRequestServiceImpl.getAllForRequestor(user.getId());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetById() {
        User user = mockUser1;
        ItemRequest itemRequest = mockItemRequest1;
        itemRequest.setRequestor(user);
        List<Item> items = List.of(mockItem1, mockItem2);
        Mockito
                .when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        Mockito
                .when(itemRepository.findAllByRequestIdOrderByIdAsc(itemRequest.getId()))
                .thenReturn(items);
        ItemRequestResponseDto result = itemRequestServiceImpl.getById(itemRequest.getId(), user.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(itemRequest.getId(), result.getId());
        Assertions.assertEquals(2, result.getItems().size());
    }

    @Test
    public void testUserNotFoundException() {
        Long userId = 1L;
        Long requestId = 1L;
        Mockito
                .when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> itemRequestServiceImpl.getById(requestId, userId));
    }

    @Test
    public void testItemRequestNotFoundException() {
        Long requestId = 1L;
        User user = mockUser1;
        Mockito
                .when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestServiceImpl.getById(requestId, user.getId()));
    }

    @Test
    public void testListItemRequestToDto() {
        ItemRequest itemRequest1 = mockItemRequest1;
        ItemRequest itemRequest2 = mockItemRequest2;
        List<ItemRequest> itemRequests = List.of(itemRequest1, itemRequest2);

        List<ItemRequestDto> result = ItemRequestMapper.listItemRequestToDto(itemRequests);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(1L, result.get(0).getId());
        Assertions.assertEquals(itemRequest1.getDescription(), result.get(0).getDescription());
        Assertions.assertEquals(2L, result.get(1).getId());
        Assertions.assertEquals(itemRequest2.getDescription(), result.get(1).getDescription());
    }

    @Test
    public void testItemRequestToDtoWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            ItemRequestMapper.itemRequestToDto(null);
        });
    }
}