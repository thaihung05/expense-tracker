package com.kei.backend.quickentry;

import static org.assertj.core.api.Assertions.assertThat;

import com.kei.backend.quickentry.QuickEntryParser.ParsedEntry;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QuickEntryParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 15);
    private final QuickEntryParser parser = new QuickEntryParser();

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', textBlock = """
            cafe 35k              | 35000
            cafe 35 k             | 35000
            cafe 2.5k             | 2500
            ăn trưa 45 nghìn      | 45000
            luong 15tr            | 15000000
            1tr2 tiền nhà         | 1200000
            1tr25 tiền nhà        | 1250000
            học phí 1.5tr         | 1500000
            máy giặt 2 triệu      | 2000000
            phở 45.000            | 45000
            phở 45,000            | 45000
            tiền điện 1.250.000   | 1250000
            gửi xe 200000         | 200000
            cafe 35               | 35000
            nạp 4g 100k           | 100000
            """)
    void readsAmount(String text, long expected) {
        assertThat(parser.parse(text, TODAY).amount()).isEqualTo(expected);
    }

    @Test
    void amountIsNullWhenTextHasNoNumber() {
        assertThat(parser.parse("đi chợ", TODAY).amount()).isNull();
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(delimiter = '|', textBlock = """
            cafe 35k              | 2026-11-15
            cafe 35k hôm nay      | 2026-11-15
            cafe 35k hôm qua      | 2026-11-14
            cafe 35k hom qua      | 2026-11-14
            Hôm Kia cafe 35k      | 2026-11-13
            grab 52k 30/10        | 2026-10-30
            grab 52k 30-10        | 2026-10-30
            grab 52k 30/10/2025   | 2025-10-30
            grab 52k 1/1/25       | 2025-01-01
            """)
    void readsDate(String text, LocalDate expected) {
        assertThat(parser.parse(text, TODAY).date()).isEqualTo(expected);
    }

    @Test
    void invalidDateIsIgnoredAndTodayIsUsed() {
        assertThat(parser.parse("cafe 35k 40/13", TODAY).date()).isEqualTo(TODAY);
    }

    @Test
    void noteIsWhatRemainsAfterRemovingAmountAndDate() {
        ParsedEntry entry = parser.parse("  Trà sữa   40k hôm qua, ", TODAY);

        assertThat(entry.note()).isEqualTo("Trà sữa");
        assertThat(entry.amount()).isEqualTo(40_000);
        assertThat(entry.date()).isEqualTo(TODAY.minusDays(1));
    }

    @Test
    void dateIsNotMistakenForAmount() {
        ParsedEntry entry = parser.parse("grab 30/10", TODAY);

        assertThat(entry.amount()).isNull();
        assertThat(entry.date()).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(entry.note()).isEqualTo("grab");
    }
}
