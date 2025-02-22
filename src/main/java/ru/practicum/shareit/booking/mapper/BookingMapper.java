package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    public static BookingDto bookingToDto(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking не может быть null");
        }

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();
    }

    public static Booking dtoToBooking(BookingDto bookingDto) {
        if (bookingDto == null) {
            throw new IllegalArgumentException("Booking не может быть null");
        }

        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(bookingDto.getItem())
                .booker(bookingDto.getBooker())
                .status(bookingDto.getStatus())
                .build();
    }

    public static List<BookingDto> bookingsToListDto(Collection<Booking> bookings) {
        return bookings.stream().map(BookingMapper::bookingToDto).collect(Collectors.toList());
    }
}
