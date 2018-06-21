package com.github.akuma1.aws.lambda.pdf.merge;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.IOUtils;
import com.github.akuma1.aws.lambda.config.AppConfig;
import com.github.akuma1.aws.lambda.pdf.util.PdfMergeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ReportMerge {
    LambdaLogger logger;

    public void handler(S3Event input, Context context) throws IOException {
        logger = context.getLogger();
        AwsServiceWrapper awsServiceWrapper = new AwsServiceWrapper();

        S3Context s3Context = new S3Context(awsServiceWrapper.s3Client(),
                awsServiceWrapper.s3EventBucket(input), awsServiceWrapper.s3EventKey(input));
        List<InputStream> pdfFilesAsStreams = getPdfFilesAsStreams(s3Context, awsServiceWrapper);
        InputStream mergedStream = PdfMergeUtil.mergePdfStreams(pdfFilesAsStreams);
        if (mergedStream != null) {
            String combinedPdfS3Key = combinedPdfS3Key(s3Context.getS3EventKey());
            logger.log("Final pdf name " + combinedPdfS3Key);
            PutObjectResult putObjectResult = awsServiceWrapper.writeS3Object(s3Context, combinedPdfS3Key,
                    mergedStream);
            logger.log("PDF merge success with eTag " + putObjectResult.getETag());

        } else {
            logger.log("Pdf merge failed, please see error log");
        }
    }

    private List<InputStream> getPdfFilesAsStreams(S3Context s3Context, AwsServiceWrapper awsServiceWrapper) throws
            IOException {
        AmazonS3 s3Client = s3Context.getS3Client();
        String s3EventBucket = s3Context.getS3EventBucket();
        String s3EventKey = s3Context.getS3EventKey();
        InputStream inputSourceData = awsServiceWrapper.fetchS3Object(s3Client, s3EventBucket, s3EventKey, logger);
        List<String> pdfS3Keys = getS3KeysFromStream(inputSourceData);
        return pdfS3Keys.stream().map(k -> awsServiceWrapper.fetchS3Object(s3Client, s3EventBucket, k, logger)).collect
                (Collectors.toList());
    }


    private String combinedPdfS3Key(String s3Key) {
        String fileName = s3Key.substring(s3Key.lastIndexOf("/") + 1);
        return new StringBuilder(AppConfig.MERGED_PDF_S3_PATH).append("/").append(fileName.replace(".txt", ".pdf")).toString();
    }

    private List<String> getS3KeysFromStream(InputStream inputSourceData) throws IOException {
        String requestedPDfFiles = IOUtils.toString(inputSourceData);
        return Arrays.asList(requestedPDfFiles.split("\\r?\\n"));
    }

}