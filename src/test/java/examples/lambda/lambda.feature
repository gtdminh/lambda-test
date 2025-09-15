Feature: Test Lambda triggered by SNS with S3 output

  Background:
    # 1. Initialize the Karate AWS helper. This is now built-in.
    # It automatically picks up AWS credentials from the environment.
    * def aws = karate.aws

    # 2. Set the region for AWS clients using the value from karate-config.js
    # The snsTopicArn and s3BucketName are also loaded automatically from the config.
    #* aws.region(awsRegion)

  Scenario: Trigger lambda via SNS and verify the output file in S3

    # 1. Define a unique identifier for this test run.
    # This ensures that our test creates a unique S3 object key,
    # preventing collisions with other test runs.
    * def uniqueId = karate.UUID()
    * def s3ObjectKey = 'output/' + uniqueId + '.json'
    * def snsTopicArn = ''

    # 2. Create the message payload to be sent to the SNS topic.
    # The Lambda function will receive this payload.
    # This payload simulates an S3 event notification.
    Given def messagePayload = 
    """
    {
      "Records": [
        {
          "eventVersion": "2.1",
          "eventSource": "aws:s3",
          "awsRegion": "#(aws.region)",
          "eventTime": "#(karate.nowIso())",
          "eventName": "ObjectCreated:Put",
          "s3": {
            "s3SchemaVersion": "1.0",
            "bucket": {
              "name": "#(s3BucketName)",
              "arn": "arn:aws:s3:::#(s3BucketName)"
            },
            "object": {
              "key": "#(s3ObjectKey)"
            }
          }
        }
      ]
    }
    """

    # 3. Publish the message to the SNS topic.
    # This action will trigger the Lambda function asynchronously.
    # The 'message' must be a string, so we use '#(messagePayload)'.
    And print 'Publishing message to SNS topic:', snsTopicArn
    * def snsResult = aws.sns.publish({ topicArn: '#(snsTopicArn)', message: '#(messagePayload)' })
    * print 'SNS publish result:', snsResult

    # 4. Poll S3 until the output file appears or we time out.
    # This is a robust way to handle the asynchronous nature of the Lambda function.
    # We wait for up to 15 seconds, retrying every 3 seconds.
    When retry(5, 3000).call read('classpath:examples/lambda/get-s3-object.js')

    # 5. Assert the contents of the S3 object.
    # The 's3Object' variable is returned from the JavaScript function called above.
    Then match s3Object.body ==
    """
    {
      "processedOrderId": "#(uniqueId)",
      "status": "SUCCESS",
      "timestamp": "#string"
    }
    """