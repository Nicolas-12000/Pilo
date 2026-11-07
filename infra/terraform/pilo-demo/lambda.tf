resource "aws_lambda_function" "worker" {
  function_name = local.lambda_name
  role          = aws_iam_role.lambda.arn
  handler       = "handler.handler"
  runtime       = "python3.12"
  architectures = ["arm64"]
  timeout       = 120
  memory_size   = 512

  filename         = var.lambda_zip_path
  source_code_hash = filebase64sha256(var.lambda_zip_path)

  environment {
    variables = {
      PILO_BACKEND_URL       = "http://${aws_eip.api.public_ip}"
      PILO_INTERNAL_API_KEY  = var.pilo_internal_api_key
      PILO_AI_PROVIDER       = "mock"
      GEMINI_MODEL           = "gemini-2.0-flash"
    }
  }
}

resource "aws_lambda_permission" "s3" {
  statement_id  = "pilo-s3-invoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.worker.function_name
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.docs.arn
  source_account = local.account_id
}

resource "aws_s3_bucket_notification" "docs" {
  bucket = aws_s3_bucket.docs.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.worker.arn
    events              = ["s3:ObjectCreated:Put"]
    filter_prefix       = "cases/"
  }

  depends_on = [aws_lambda_permission.s3]
}
