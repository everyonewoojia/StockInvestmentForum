package com.stock.forum.service;

import com.stock.forum.dto.OcrDtos;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class OcrParserTest {
    @Test
    void parsesCommonPrescriptionLines() {
        OcrParser parser = new OcrParser();

        OcrDtos.MedicineInfoResponse result = parser.parse(Arrays.asList(
                "布洛芬缓释胶囊",
                "每日3次",
                "每次1粒",
                "饭后服用",
                "疗程7天"
        ));

        assertThat(result.medicineName).isEqualTo("布洛芬缓释胶囊");
        assertThat(result.dailyTimes).isEqualTo(3);
        assertThat(result.dose).isEqualTo("每次1粒");
        assertThat(result.takeTime).isEqualTo("饭后");
        assertThat(result.cycle).isEqualTo(7);
    }
}
