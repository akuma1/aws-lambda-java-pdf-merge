package com.github.akuma1.aws.lambda.pdf.merge;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.github.akuma1.aws.lambda.config.AppConfig;

class S3Context {
    private AmazonS3 s3Client;
    private String s3EventBucket;
    private String s3EventKey;

    S3Context(S3Event input) {
        this.s3Client = AmazonS3ClientBuilder.standard().withRegion(AppConfig.AWS_S3_REGION).build();
        S3EventNotification.S3EventNotificationRecord record = input.getRecords().get(0);
        this.s3EventBucket = record.getS3().getBucket().getName();
        this.s3EventKey = record.getS3().getObject().getKey();
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }

    public String getS3EventBucket() {
        return s3EventBucket;
    }

    public String getS3EventKey() {
        return s3EventKey;
    }
}