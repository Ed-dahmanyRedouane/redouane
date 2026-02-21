package com.bookshop.service;

import com.bookshop.dto.BookRequest;
import com.bookshop.dto.BookResponse;
import com.bookshop.entity.Book;
import com.bookshop.entity.Category;
import com.bookshop.repository.BookRepository;
import com.bookshop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public Page<BookResponse> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAllByOrderByIdDesc(pageable)
                .map(this::convertToResponse);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'ID: " + id));
        return convertToResponse(book);
    }

    @Transactional
    public BookResponse createBook(BookRequest bookRequest) {
        Category category = categoryRepository.findById(bookRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + bookRequest.getCategoryId()));

        Book book = Book.builder()
                .title(bookRequest.getTitle())
                .author(bookRequest.getAuthor())
                .price(bookRequest.getPrice())
                .stock(bookRequest.getStock())
                .description(bookRequest.getDescription())
                .category(category)
                .build();

        Book savedBook = bookRepository.save(book);
        return convertToResponse(savedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Livre non trouvé avec l'ID: " + id);
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest bookRequest) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'ID: " + id));
        
        Category category = categoryRepository.findById(bookRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + bookRequest.getCategoryId()));

        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setPrice(bookRequest.getPrice());
        book.setStock(bookRequest.getStock());
        book.setDescription(bookRequest.getDescription());
        book.setCategory(category);

        Book updatedBook = bookRepository.save(book);
        return convertToResponse(updatedBook);
    }

    private BookResponse convertToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .stock(book.getStock())
                .description(book.getDescription())
                .category(convertCategoryToResponse(book.getCategory()))
                .build();
    }

    private com.bookshop.dto.CategoryResponse convertCategoryToResponse(Category category) {
        return com.bookshop.dto.CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
