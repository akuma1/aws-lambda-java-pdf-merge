package com.github.akuma1.aws.lambda.pdf.merge;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({S3Context.class, AwsServiceWrapper.class})
public class S3ContextSpec {

    @Test
    public void s3ContextInitializeContextFields() throws Exception {
        AmazonS3 s3Client = mock(AmazonS3.class);
        S3Context context = new S3Context(s3Client, "BucketName", "/fileKey");
        assertEquals("BucketName", context.getS3EventBucket());
        assertEquals("/fileKey", context.getS3EventKey());
        assertEquals(s3Client, context.getS3Client());


    }
}
