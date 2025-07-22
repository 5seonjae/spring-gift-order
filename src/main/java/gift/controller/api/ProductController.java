package gift.controller.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import gift.dto.api.ProductCreateRequestDto;
import gift.dto.api.ProductUpdateRequestDto;
import gift.entity.Product;
import gift.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody @Valid ProductCreateRequestDto dto) {
        Product product = new Product(dto.getName(), dto.getPrice(), dto.getImageUrl());
        Product saved = productService.registerProduct(product);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // 상품 전체 조회
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
        @PageableDefault(size = 5, sort = "id", direction = DESC) Pageable pageable
    ) {
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);  // 200 OK + JSON 배열
    }

    // 상품 개별 조회
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product); // 200 OK
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable long id, @RequestBody @Valid ProductUpdateRequestDto updateRequestDto) {
        Product updated = productService.updateProduct(id, updateRequestDto);
        return ResponseEntity.ok(updated); // 200 OK
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
