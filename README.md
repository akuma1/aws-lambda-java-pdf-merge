# aws-lambda-java-pdf-merge 
Merge pdf files from S3 and store resulted pdf back to S3.

Please note:

* This lambda function uses S3Event to get bucket name and input txt file name.

* Input text file should contain path of pdf files, please see inputFileExample.txt file for reference.

*  Code uses same bucket for merged pdf.

* You can define path of merged pdf file and s3 region in AppConfig.java file.

* File name of merged pdf will be same as inputFile name, for example if input file is myFile.txt then merged pdf name
will be myFile.pdf



