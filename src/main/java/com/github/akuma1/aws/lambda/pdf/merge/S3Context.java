package com.github.akuma1.aws.lambda.pdf.merge;

import com.amazonaws.services.s3.AmazonS3;

class S3Context {
    private AmazonS3 s3Client;
    private String s3EventBucket;
    private String s3EventKey;

    S3Context(AmazonS3 s3Client, String s3EventBucket, String s3EventKey) {
        this.s3Client = s3Client;
        this.s3EventBucket = s3EventBucket;
        this.s3EventKey = s3EventKey;
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