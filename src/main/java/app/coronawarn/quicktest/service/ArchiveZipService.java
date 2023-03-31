/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.CsvUploadConfig;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.AesVersion;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveZipService {

    private final CsvUploadConfig csvConfig;

    private final AmazonS3 s3Client;

    /**
     * Create an encrypted ZIP file in OBS Bucket for given partnerIds.
     *
     * @param filename   Filename of the ZIP file to be created in Bucket
     * @param partnerIds List of the PartnerIds to be added to ZIP file.
     * @param password   password to encrypt ZIP file
     */
    public void createZip(String filename, List<String> partnerIds, String password)
        throws ArchiveZipServiceException, IOException {


        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        zipParameters.setAesVersion(AesVersion.TWO);
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);

        try (
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipStream = new ZipOutputStream(byteArrayOutputStream, password.toCharArray())) {
            for (String partnerId : partnerIds) {
                String csvFileName = partnerId + ".csv";
                S3Object s3Object;

                try {
                    s3Object = s3Client.getObject(csvConfig.getBucketName(), csvFileName);
                } catch (AmazonServiceException e) {
                    if (e.getStatusCode() == 404) {
                        log.error("Could not find {} in bucket.", csvFileName);
                        throw new ArchiveZipServiceException(csvFileName,
                            ArchiveZipServiceException.Reason.CSV_FILE_NOT_FOUND);
                    } else {
                        log.error("Unexpected error when downloading CSV {} from bucket. Status Code: {}",
                            csvFileName, e.getStatusCode());
                        throw new ArchiveZipServiceException(csvFileName,
                            ArchiveZipServiceException.Reason.UNEXPECTED_ERROR);
                    }
                }

                byte[] buff = new byte[4096];
                int readLen;

                zipParameters.setFileNameInZip(csvFileName);
                zipStream.putNextEntry(zipParameters);
                try (InputStream inputStream = s3Object.getObjectContent()) {
                    while ((readLen = inputStream.read(buff)) != -1) {
                        zipStream.write(buff, 0, readLen);
                    }
                }
                zipStream.closeEntry();
            }
            zipStream.close();

            byte[] zipBytes = byteArrayOutputStream.toByteArray();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            objectMetadata.setContentLength(zipBytes.length);
            s3Client.putObject(
                csvConfig.getBucketName(),
                filename,
                new ByteArrayInputStream(zipBytes),
                objectMetadata);
        } catch (ArchiveZipServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating ZIP file.", e);
            throw new ArchiveZipServiceException(e.getMessage(), ArchiveZipServiceException.Reason.UNEXPECTED_ERROR);
        }
    }

    /**
     * Create a presigned URL to download a file from OBS bucket.
     *
     * @param filename name of the file in bucket.
     * @return URL to download file.
     * @throws ArchiveZipServiceException if creation went wrong.
     */
    public URL getDownloadUrl(String filename, Date expiration) throws ArchiveZipServiceException {

        if (!s3Client.doesObjectExist(csvConfig.getBucketName(), filename)) {
            throw new ArchiveZipServiceException(filename, ArchiveZipServiceException.Reason.ZIP_FILE_NOT_FOUND);
        }

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
            new GeneratePresignedUrlRequest(csvConfig.getBucketName(), filename)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        URL presignedUrl;
        try {
            presignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        } catch (SdkClientException e) {
            throw new ArchiveZipServiceException(e.getMessage(), ArchiveZipServiceException.Reason.UNEXPECTED_ERROR);
        }

        return presignedUrl;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ArchiveZipServiceException extends Exception {

        private final String message;
        private final Reason reason;

        public enum Reason {
            CSV_FILE_NOT_FOUND,
            ZIP_FILE_NOT_FOUND,
            UNEXPECTED_ERROR
        }
    }

}
