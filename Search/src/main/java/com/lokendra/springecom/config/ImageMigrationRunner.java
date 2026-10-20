package com.lokendra.springecom.config;

import com.lokendra.springecom.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImageMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ImageMigrationRunner.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ImageStorageService imageStorageService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            
            String sql = "SELECT id, image_type, image_data FROM product WHERE image_stored_name IS NULL AND image_data IS NOT NULL";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            if (rows.isEmpty()) {
                return;
            }

            log.info("Found {} legacy products with image BLOBs in MySQL. Migrating to disk storage...", rows.size());
            int count = 0;

            for (Map<String, Object> row : rows) {
                int id = ((Number) row.get("id")).intValue();
                String mimeType = (String) row.get("image_type");
                byte[] data = (byte[]) row.get("image_data");

                if (data != null && data.length > 0) {
                    if (mimeType == null || mimeType.isBlank()) {
                        mimeType = "image/jpeg";
                    }
                    String storedName = imageStorageService.storeBytes(data, mimeType);
                    jdbcTemplate.update("UPDATE product SET image_stored_name = ? WHERE id = ?", storedName, id);
                    count++;
                    log.info("Migrated product id={} image to file: {}", id, storedName);
                }
            }

            log.info("Successfully migrated {} product images from MySQL to disk.", count);

        } catch (Exception e) {
            log.warn("Image migration skipped or not needed: {}", e.getMessage());
        }
    }
}
