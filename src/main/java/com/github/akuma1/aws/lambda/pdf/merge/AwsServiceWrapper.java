package com.github.akuma1.aws.lambda.pdf.merge;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.github.akuma1.aws.lambda.config.AppConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AwsServiceWrapper {

    public AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.standard().withRegion(AppConfig.AWS_S3_REGION).build();

    }

    public String s3EventBucket(S3Event input) {

        return input.getRecords().get(0).getS3().getBucket().getName();

    }

    public String s3EventKey(S3Event input) {

        return input.getRecords().get(0).getS3().getObject().getKey();

    }

    public InputStream fetchS3Object(AmazonS3 s3Client, String s3Bucket, String s3Key, LambdaLogger logger) {
        S3Object s3Obj = null;
        InputStream is;
        try {
            s3Obj = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
            S3ObjectInputStream stream = s3Obj.getObjectContent();
            ByteArrayOutputStream temp = new ByteArrayOutputStream();
            IOUtils.copy(stream, temp);
            is = new ByteArrayInputStream(temp.toByteArray());
        } catch (Throwable e) {
            logger.log("Error in fetching  pdf = " + s3Key);
            logger.log("fetchS3Object error: {}" + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (s3Obj != null) {
                try {
                    s3Obj.close();
                } catch (IOException e) {
                    logger.log("Unable to close S3 object: {}" + e.getMessage());
                }
            }
        }
        return is;
    }

    public PutObjectResult writeS3Object(S3Context s3Context, String combinedPdfS3Key, InputStream mergedStream)
            throws IOException {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(mergedStream.available());
        PutObjectRequest putObjectRequest = new PutObjectRequest(s3Context.getS3EventBucket(), combinedPdfS3Key, mergedStream, meta);
        return s3Context.getS3Client().putObject(putObjectRequest);

    }
}
