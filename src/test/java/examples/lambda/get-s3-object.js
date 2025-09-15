function() {
  // This function is called repeatedly by the `retry` in the feature file.
  // It attempts to fetch the object from S3.
  // If the object is not found, aws.s3.get() will throw an exception,
  // causing the retry mechanism to try again.

  karate.log('Attempting to get S3 object:', s3ObjectKey, 'from bucket:', s3BucketName);

  // The variables s3BucketName and s3ObjectKey are available
  // because they were defined in the calling feature file's scope.
  var result = aws.s3.get(s3BucketName, s3ObjectKey);

  // If the call succeeds, the object is returned to the feature file.
  return { body: result.body };
}