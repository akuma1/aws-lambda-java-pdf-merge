package com.github.akuma1.aws.lambda.pdf.merge;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.github.akuma1.aws.lambda.pdf.util.PdfMergeUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReportMerge.class, S3Context.class, PdfMergeUtil.class, AwsServiceWrapper.class})

public class ReportMergeSpec {

    TestContext testContext;
    AmazonS3 s3Client;
    S3Context s3Context;
    ReportMerge reportMerge = new ReportMerge();
    AwsServiceWrapper awsServiceWrapper;
    ReportMerge reportMergeSpy = PowerMockito.spy(reportMerge);

    S3Event s3Event;
    PutObjectResult putObjectResult;

    @Before
    public void setUp() throws Exception {
        testContext = new TestContext();
        awsServiceWrapper = mock(AwsServiceWrapper.class);
        s3Context = mock(S3Context.class);
        when(s3Context.getS3EventKey()).thenReturn("/file.txt");
        when(s3Context.getS3EventBucket()).thenReturn("s3Bucket");
        s3Client = mock(AmazonS3.class);
        when(s3Context.getS3Client()).thenReturn(s3Client);

    }


    @Test
    public void handlerShouldCallwriteS3Object() throws Exception {
        s3Event = mock(S3Event.class);
        PowerMockito.whenNew(AwsServiceWrapper.class).withNoArguments().thenReturn(awsServiceWrapper);
        when(awsServiceWrapper.s3Client()).thenReturn(s3Client);
        when(awsServiceWrapper.s3EventKey(anyObject())).thenReturn("/file.txt");
        when(awsServiceWrapper.s3EventBucket(anyObject())).thenReturn("s3Bucket");
        putObjectResult = mock(PutObjectResult.class);
        PowerMockito.whenNew(S3Context.class).withAnyArguments().thenReturn(s3Context);
        InputStream is = new ByteArrayInputStream("testString".getBytes());
        List<InputStream> listOfSteams = new ArrayList<InputStream>();
        listOfSteams.add(is);

        PowerMockito.doReturn(listOfSteams).when(reportMergeSpy, "getPdfFilesAsStreams", s3Context, awsServiceWrapper);
        when(awsServiceWrapper.writeS3Object(anyObject(), anyObject(), anyObject())).thenReturn(putObjectResult);

        PowerMockito.mockStatic(PdfMergeUtil.class);
        InputStream mergedContent = new ByteArrayInputStream("mergedContent".getBytes());

        when(PdfMergeUtil.mergePdfStreams(anyObject())).thenReturn(mergedContent);

        reportMergeSpy.handler(s3Event, testContext);
        verify(awsServiceWrapper, times(1)).writeS3Object(s3Context, "tmp/pdf/file.pdf", mergedContent);

    }


    @Test
    public void getPdfFilesAsStreamsWhenS3ContextIsProvided() throws Exception {
        InputStream is = new ByteArrayInputStream("/file1.pdf\n/file2.pdf".getBytes());
        InputStream is1 = new ByteArrayInputStream("file1.pdf".getBytes());
        InputStream is2 = new ByteArrayInputStream("file2.pdf".getBytes());

        awsServiceWrapper = mock(AwsServiceWrapper.class);

        System.out.println(s3Context.getS3Client() + "s3Bucket" + "/file.txt" + null);

        when(awsServiceWrapper.fetchS3Object(s3Client, "s3Bucket", "/file.txt", null)).thenReturn(is);
        when(awsServiceWrapper.fetchS3Object(s3Client, "s3Bucket", "/file1.pdf", null)).thenReturn(is1);
        when(awsServiceWrapper.fetchS3Object(s3Client, "s3Bucket", "/file2.pdf", null)).thenReturn(is2);

        List<InputStream> result = Whitebox.invokeMethod(reportMergeSpy, "getPdfFilesAsStreams", s3Context, awsServiceWrapper);
        System.out.println(result);
        assertEquals(2, result.size());
        assertEquals(is1, result.get(0));
        assertEquals(is2, result.get(1));
    }


    @Test
    public void combinedPdfS3KeyShouldReturnPdfFileKey() throws Exception {
        String result = Whitebox.invokeMethod(new ReportMerge(), "combinedPdfS3Key", "tmp/pdf/file.txt");
        assertEquals("tmp/pdf/file.pdf", result);
    }


    @Test
    public void getS3KeysFromStreamShouldReturnListOfPdfFileNames() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("file1.pdf\nfile2.pdf".getBytes());
        List<String> result = Whitebox.invokeMethod(new ReportMerge(), "getS3KeysFromStream", inputStream);
        assertEquals(2, result.size());
        assertEquals("file1.pdf", result.get(0));
        assertEquals("file2.pdf", result.get(1));

    }

}
