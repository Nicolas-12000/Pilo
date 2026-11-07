resource "aws_s3_bucket" "docs" {
  bucket = local.bucket_name
}

resource "aws_s3_bucket_public_access_block" "docs" {
  bucket = aws_s3_bucket.docs.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "docs" {
  bucket = aws_s3_bucket.docs.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_cors_configuration" "docs" {
  bucket = aws_s3_bucket.docs.id
  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT", "GET", "HEAD"]
    allowed_origins = distinct(concat(
      ["http://localhost:3000", "http://127.0.0.1:3000"],
      [for o in split(",", var.pilo_cors_origins) : trimspace(o) if trimspace(o) != ""]
    ))
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}
