package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {
    private User mockUser1;
    private User mockUser2;
    private Item mockItem1;
    private Booking mockBooking1;
    private Booking mockBooking2;

    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    BookingServiceManager bookingServiceManager;

    private MockitoSession session;

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        bookingServiceManager = new BookingServiceManager(bookingRepository, itemRepository, userRepository);
        mockUser1 = new User(1L, "Иван", "ivan@yandex.ru");
        mockUser2 = new User(2L, "Петр", "petr@yandex.ru");
        mockItem1 = new Item(1L, "Книга", "Книга.Описание", true, mockUser1, 1L);
        mockBooking1 = new Booking(1L, LocalDateTime.of(2021, 12, 12, 1, 1),
                LocalDateTime.of(2021, 12, 22, 1, 1), mockItem1, mockUser2,
                StatusType.APPROVED);
        mockBooking2 = new Booking(2L, LocalDateTime.of(2024, 12, 12, 1, 1),
                LocalDateTime.of(2024, 12, 22, 1, 1), mockItem1, mockUser2,
                StatusType.APPROVED);
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    public void createTest() {
        User user = mockUser2;
        Item item = mockItem1;
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        bookingRequestDto.setStart(start);
        bookingRequestDto.setEnd(end);
        Booking booking = BookingMapper.requestToBooking(bookingRequestDto);
        booking.setItem(item);
        booking.getItem().setAvailable(true);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);
        BookingResponseDto result = bookingServiceManager.createBooking(bookingRequestDto, user.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void createBookingWhenItemIsNotAvailableShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        User user = mockUser1;
        Item item = mockItem1;
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item.getId(), start, end);
        item.setOwner(user);
        item.setAvailable(false);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, user.getId());
        });
    }

    @Test
    void createBookingWhenStartIsAfterEndShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        User user = mockUser1;
        Item item = mockItem1;
        item.setOwner(user);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item.getId(), start, end);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, user.getId());
        });
    }

    @Test
    void createBookingWhenStartIsBeforeNowShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, start, end);
        User user = mockUser1;
        Item item = mockItem1;
        item.setOwner(user);
        item.setAvailable(true);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, 1L);
        });
    }

    @Test
    void createBookingWhenUserIsOwnerShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, start, end);
        User user = mockUser1;
        Item item = mockItem1;
        item.setOwner(user);
        item.setAvailable(true);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, 1L);
        });
    }

    @Test
    public void createBookingInvalidStartAndEndTimesThrowsBadRequestException() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        bookingRequestDto.setStart(LocalDateTime.now().plusHours(2));
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(1));
        User user = mockUser1;
        Item item = mockItem1;
        item.setAvailable(true);
        item.setOwner(user);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, 1L);
        });
    }

    @Test
    void testConfirmBookingSuccess() {
        Booking booking = mockBooking1;
        booking.setBooker(mockUser2);
        booking.setStatus(StatusType.WAITING);
        booking.getItem().setOwner(mockUser1);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(Mockito.any())).thenReturn(true);
        bookingServiceManager.confirm(booking.getId(), booking.getItem().getOwner().getId(), true);
        Assertions.assertEquals(StatusType.APPROVED, booking.getStatus());
        Mockito.verify(bookingRepository, times(1)).findById(1L);
        Mockito.verify(userRepository, times(1)).existsById(1L);
    }

    @Test
    public void testGetByIdExistingBooking() {
        Booking booking = mockBooking1;
        Long bookingId = 1L;
        Long userId = 1L;
        BookingResponseDto expectedResponse = BookingMapper.bookingToResponse(booking);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        BookingResponseDto actualResponse = bookingServiceManager.getById(bookingId, userId);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testGetByIdNonExistingBooking() {
        Long bookingId = 1L;
        Long userId = 1L;
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(null);
        assertThrows(NullPointerException.class, () -> {
            bookingServiceManager.getById(bookingId, userId);
        });
    }

    @Test
    public void testGetByIdDataAccess() {
        Booking booking = mockBooking1;
        booking.setBooker(mockUser2);
        booking.getItem().setOwner(mockUser1);
        Long bookingId = 1L;
        Long userId = 3L;
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.getById(bookingId, userId);
        }, "Booking на найден");
    }

    @Test
    public void testGetByIdCorrectData() {
        Booking booking = mockBooking1;
        Long bookingId = 1L;
        Long userId = 1L;
        BookingResponseDto expectedResponse = BookingMapper.bookingToResponse(booking);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        BookingResponseDto actualResponse = bookingServiceManager.getById(bookingId, userId);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testDtoToBooking() {
        BookingDto bookingDto = BookingMapper.bookingToDto(mockBooking1);
        Booking booking = BookingMapper.dtoToBooking(bookingDto);

        Assertions.assertEquals(bookingDto.getId(), booking.getId());
        Assertions.assertEquals(bookingDto.getStart(), booking.getStart());
        Assertions.assertEquals(bookingDto.getEnd(), booking.getEnd());
        Assertions.assertEquals(bookingDto.getItem(), booking.getItem());
        Assertions.assertEquals(bookingDto.getBooker(), booking.getBooker());
        Assertions.assertEquals(bookingDto.getStatus(), booking.getStatus());
    }

    @Test
    public void testCreateBooking_ItemNotAvailable() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        Long userId = 1L;
        Item item = mockItem1;
        item.setAvailable(false);
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, userId));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testCreateBooking_StartAfterEnd() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().minusDays(1));
        Long userId = 1L;
        Item item = mockItem1;
        item.setAvailable(true);
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, userId));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testCreateBooking_StartInPast() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        bookingRequestDto.setStart(LocalDateTime.now().minusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(1));
        Long userId = 1L;
        Item item = mockItem1;
        item.setAvailable(true);
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, userId));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testCreateBooking_UserIsOwner() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        Long userId = 1L;
        Item item = mockItem1;
        item.setAvailable(true);
        item.setOwner(new User(userId, "Test User", "test@email.com"));
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, userId));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testConfirmBooking_BookingAlreadyApprovedOrRejected() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.APPROVED);
        Long bookingId = 1L;
        Long userOwnerId = 1L;
        Mockito.when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userOwnerId))
                .thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.confirm(bookingId, userOwnerId, true));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testConfirmBooking_BookingNotWaiting() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.REJECTED);
        Long bookingId = 1L;
        Long userOwnerId = 1L;
        Mockito.when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userOwnerId))
                .thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.confirm(bookingId, userOwnerId, true));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testConfirmBooking_UserNotOwner() {
        Booking booking = mockBooking1;
        Long bookingId = 1L;
        Long userOwnerId = 2L;
        Mockito.when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userOwnerId))
                .thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.confirm(bookingId, userOwnerId, true));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }
}