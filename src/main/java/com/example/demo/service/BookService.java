package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.BookCreateRequest;
import com.example.demo.dto.BookDto;
import com.example.demo.dto.BookUpdateRequest;
import com.example.demo.entity.Book;
import com.example.demo.repository.BookRepository;

/**
 * 書籍的業務邏輯層。
 *
 * <p>對外（Controller）只接收、回傳 DTO，Entity 只在 Service 與 Repository 之間使用。
 * 這樣資料表結構改變時，不會直接影響 API 的格式。</p>
 */
@Service
public class BookService {

    private final BookRepository bookRepository;

    /** 建構子注入（推薦寫法）：依賴可以宣告成 final，也方便在單元測試中傳入 Mock 物件。 */
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 將 Entity 轉換成 DTO（之後也可以用 MapStruct 等工具自動產生這段程式）
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

    /** 查詢所有書籍：findAll() 取得 Entity 清單後，用 Stream 逐筆轉成 DTO。 */
    public List<BookDto> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 依 id 查詢書籍。回傳 Optional：查不到時為 Optional.empty()，
     * 由 Controller 決定要回傳 404。
     */
    public Optional<BookDto> getBookById(Integer id) {
        return bookRepository.findById(id).map(this::toDto);
    }

    /** 新增書籍：Request DTO → Entity → save()（INSERT）→ 回傳含新 id 的 DTO。 */
    public BookDto createBook(BookCreateRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setListPrice(request.getListPrice());
        book.setSalePrice(request.getSalePrice());
        return toDto(bookRepository.save(book));
    }

    /**
     * 更新書籍：先查出原本的資料，修改欄位後再 save()（因為有 id，所以是 UPDATE）。
     * 找不到該 id 時回傳 Optional.empty()。
     */
    public Optional<BookDto> updateBook(Integer id,BookUpdateRequest request) {
        // Optional.map：有值時才執行裡面的 lambda，沒值時直接回傳 empty
        return bookRepository.findById(id).map(b -> {
            b.setTitle(request.getTitle());
            b.setAuthor(request.getAuthor());
            b.setDescription(request.getDescription());
            b.setListPrice(request.getListPrice());
            b.setSalePrice(request.getSalePrice());
            return toDto(bookRepository.save(b));
        });
    }

    /** 刪除書籍；回傳 false 代表該 id 不存在，Controller 會回傳 404。 */
    public boolean deleteBook(Integer id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
