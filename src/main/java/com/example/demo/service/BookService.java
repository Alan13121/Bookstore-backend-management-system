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

/**
 * 書籍的商業邏輯。
 *
 * <p>分層架構：Controller（接收請求）→ Service（商業邏輯）→ Repository（存取資料庫）。
 * 對外只接收、回傳 DTO，Entity 只在 Service 與 Repository 之間使用。</p>
 *
 * <p>{@code @Transactional(readOnly = true)} 加在類別上，表示所有方法預設在「唯讀交易」中執行
 * （Hibernate 會省略變更檢查，效能較好）；會寫入資料的方法再個別標上 {@code @Transactional} 覆蓋成可寫入。
 * 同一個交易中的操作要嘛全部成功、要嘛全部回滾（rollback）。</p>
 */
@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    /** 建構子注入（推薦寫法）：依賴可以宣告成 final，單元測試時也方便傳入 Mock 物件。 */
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /** 查詢所有書籍：取得 Entity 清單後，用 Stream 逐筆轉成 DTO。 */
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /** 依 id 查詢書籍；找不到時丟出 ResourceNotFoundException → 404。 */
    public BookResponse getBookById(Integer id) {
        return toResponse(findBook(id));
    }

    /** 新增書籍：Request DTO → Entity → save()（INSERT）→ 回傳含新 id 的 DTO。 */
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

    /**
     * 更新書籍（整筆覆蓋）。
     *
     * <p>小知識：在交易中查出來的 Entity 是「受管理（managed）」狀態，修改欄位後即使不呼叫 save()，
     * 交易結束時 Hibernate 也會自動 UPDATE（稱為 dirty checking）。這裡保留 save() 讓意圖更明確。</p>
     */
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

    /** 刪除書籍；先確認存在，否則丟出 404。 */
    @Transactional
    public void deleteBook(Integer id) {
        if (!bookRepository.existsById(id)) {
            throw notFound(id);
        }
        bookRepository.deleteById(id);
    }

    /** 共用的查詢方法：查不到就丟例外，讓各個 public 方法不用重複寫判斷。 */
    private Book findBook(Integer id) {
        return bookRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private ResourceNotFoundException notFound(Integer id) {
        return new ResourceNotFoundException("找不到書籍，ID: " + id);
    }

    /** Entity → DTO（之後也可以用 MapStruct 等工具自動產生這段程式）。 */
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
