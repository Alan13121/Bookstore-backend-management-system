package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.BookCreateRequest;
import com.example.demo.dto.BookResponse;
import com.example.demo.dto.BookUpdateRequest;
import com.example.demo.entity.Book;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BookRepository;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public BookResponse getBookById(Integer id) {
        return toResponse(findBook(id));
    }

    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setListPrice(request.getListPrice());
        book.setSalePrice(request.getSalePrice());
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse updateBook(Integer id, BookUpdateRequest request) {
        Book book = findBook(id);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setListPrice(request.getListPrice());
        book.setSalePrice(request.getSalePrice());
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Integer id) {
        if (!bookRepository.existsById(id)) {
            throw notFound(id);
        }
        bookRepository.deleteById(id);
    }

    private Book findBook(Integer id) {
        return bookRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private ResourceNotFoundException notFound(Integer id) {
        return new ResourceNotFoundException("找不到書籍，ID: " + id);
    }

    private BookResponse toResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setDescription(book.getDescription());
        response.setListPrice(book.getListPrice());
        response.setSalePrice(book.getSalePrice());
        return response;
    }
}
