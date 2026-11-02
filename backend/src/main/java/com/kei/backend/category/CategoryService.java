package com.kei.backend.category;

import com.kei.backend.common.ApiException;
import com.kei.backend.common.TextNormalizer;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepo;

    public CategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<CategoryResponse> getAll(Long userId) {
        return categoryRepo.findVisible(userId).stream().map(CategoryResponse::from).toList();
    }

    public List<CategoryResponse> getByType(Long userId, CategoryType type) {
        return categoryRepo.findVisibleByType(userId, type).stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse getById(Long userId, Long id) {
        return CategoryResponse.from(findVisible(userId, id));
    }

    /** Dung chung cho cac service khac (giao dich, ngan sach) de kiem tra quyen dung danh muc. */
    public Category findVisible(Long userId, Long id) {
        return categoryRepo.findById(id)
                .filter(c -> c.isVisibleTo(userId))
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy danh mục " + id));
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        String name = request.name().trim();
        String key = TextNormalizer.normalize(name);
        boolean duplicated = categoryRepo.findVisible(userId).stream()
                .anyMatch(c -> TextNormalizer.normalize(c.getName()).equals(key));
        if (duplicated) {
            throw ApiException.conflict("Danh mục \"" + name + "\" đã tồn tại");
        }
        String icon = request.icon() == null || request.icon().isBlank() ? "🏷️" : request.icon().trim();
        return CategoryResponse.from(categoryRepo.save(new Category(userId, name, request.type(), icon)));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Category category = findVisible(userId, id);
        if (category.isDefault()) {
            throw ApiException.badRequest("Không thể xóa danh mục mặc định");
        }
        if (categoryRepo.isUsedByTransactions(id)) {
            throw ApiException.conflict("Danh mục đang có giao dịch, hãy xóa hoặc chuyển giao dịch trước");
        }
        categoryRepo.delete(category);
    }
}
