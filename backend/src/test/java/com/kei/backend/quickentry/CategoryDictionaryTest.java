package com.kei.backend.quickentry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CategoryDictionaryTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', textBlock = """
            cafe                 | Ăn uống
            Trà sữa Phúc Long    | Ăn uống
            đổ xăng              | Đi lại
            tiền nhà tháng 11    | Nhà cửa
            tiền nước            | Hóa đơn
            nước mía             | Ăn uống
            mua áo shopee        | Mua sắm
            lương tháng 10       | Lương
            lì xì                | Thu nhập khác
            """)
    void picksTheMostSpecificKeyword(String note, String expectedCategory) {
        assertThat(CategoryDictionary.lookup(note))
                .hasValueSatisfying(m -> assertThat(m.categoryName()).isEqualTo(expectedCategory));
    }

    @Test
    void keywordMustBeAWholeWord() {
        // "an" nam trong "ban" nhung khong phai 1 tu rieng -> khong duoc khop
        assertThat(CategoryDictionary.lookup("bàn ghế")).isEmpty();
    }

    @Test
    void learnerKeepsFirstThreeWordsWithoutAccentsOrNumbers() {
        assertThat(KeywordLearner.toKeyword("Gửi tiền mẹ tháng 11")).isEqualTo("gui tien me");
        assertThat(KeywordLearner.toKeyword("2 ly trà sữa")).isEqualTo("ly tra sua");
        assertThat(KeywordLearner.toKeyword("   ")).isEmpty();
    }
}
