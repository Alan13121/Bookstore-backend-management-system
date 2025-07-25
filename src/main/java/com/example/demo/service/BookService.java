package com.example.demo.service;

import com.example.demo.Dto.BookCreateRequest;
import com.example.demo.Dto.BookDto;
import com.example.demo.Dto.BookUpdateRequest;
import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 將 Entity 轉換成 DTO
    private BookDto toDto(Book book) {
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setDescription(book.getDescription());
        dto.setListPrice(book.getListPrice());
        dto.setSalePrice(book.getSalePrice());
        return dto;
    }

    public List<BookDto> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<BookDto> getBookById(Integer id) {
        return bookRepository.findById(id).map(this::toDto);
    }

    public BookDto createBook(BookCreateRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setListPrice(request.getListPrice());
        book.setSalePrice(request.getSalePrice());
        return toDto(bookRepository.save(book));
    }

    public Optional<BookDto> updateBook(BookUpdateRequest request) {
        return bookRepository.findById(request.getId()).map(b -> {
            b.setTitle(request.getTitle());
            b.setAuthor(request.getAuthor());
            b.setDescription(request.getDescription());
            b.setListPrice(request.getListPrice());
            b.setSalePrice(request.getSalePrice());
            return toDto(bookRepository.save(b));
        });
    }

    public boolean deleteBook(Integer id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
