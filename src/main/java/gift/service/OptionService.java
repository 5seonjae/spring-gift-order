package gift.service;

import gift.dto.api.OptionRequestDto;
import gift.dto.api.OptionResponseDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.exception.OptionProductMismatchException;
import gift.repository.OptionRepository;
import gift.repository.ProductRepository;
import java.util.NoSuchElementException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OptionService {

    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public OptionService(OptionRepository optionRepository, ProductRepository productRepository) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<OptionResponseDto> getOptionList(Long productId, Pageable pageable) {
        return optionRepository
            .findAllByProductId(productId, pageable)
            .map(OptionResponseDto::of);
    }

    public Option addOption(Long productId, OptionRequestDto optionRequestDto) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        optionRepository.findByProductIdAndName(productId, optionRequestDto.getName())
            .ifPresent(option -> {
                throw new DuplicateKeyException("이미 존재하는 옵션입니다.");
            });

        return optionRepository.save(new Option(
            product,
            optionRequestDto.getName(),
            optionRequestDto.getQuantity()
        ));
    }

    public Option updateOption(Long productId, Long optionId, OptionRequestDto optionRequestDto) {
        Option option = optionRepository.findById(optionId)
            .orElseThrow(() -> new NoSuchElementException("옵션을 찾을 수 없습니다."));

        if (!option.belongsTo(productId)) {
            Product otherProduct = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

            throw new OptionProductMismatchException(
                option.getName() + " 옵션은 " + otherProduct.getName() + " 상품에 속하지 않습니다.");
        }

        optionRepository.findByProductIdAndName(option.getProduct().getId(), optionRequestDto.getName())
            .ifPresent(o -> {
                throw new DuplicateKeyException("이미 존재하는 옵션입니다.");
            });

        Option updatedOption = new Option(
            optionId,
            option.getProduct(),
            optionRequestDto.getName(),
            optionRequestDto.getQuantity()
        );

        return optionRepository.save(updatedOption);
    }

    public void subtractQuantity(Long optionId, int qty) {
        Option option = optionRepository.findById(optionId)
            .orElseThrow(() -> new NoSuchElementException("옵션을 찾을 수 없습니다."));

        // 엔티티 내부에서 검증 & 차감
        option.subtract(qty);
        // Dirty Checking → 트랜잭션 종료 시 UPDATE
    }

    public void deleteOption(Long optionId) {
        optionRepository.findById(optionId)
            .orElseThrow(() -> new NoSuchElementException("옵션을 찾을 수 없습니다."));

        optionRepository.deleteById(optionId);
    }
}
