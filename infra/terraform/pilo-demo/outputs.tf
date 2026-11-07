output "elastic_ip" {
  value = aws_eip.api.public_ip
}

output "ec2_instance_id" {
  value = aws_instance.api.id
}

output "rds_endpoint" {
  value = aws_db_instance.main.address
}

output "s3_bucket" {
  value = aws_s3_bucket.docs.id
}

output "lambda_function_name" {
  value = aws_lambda_function.worker.function_name
}

output "github_actions_role_arn" {
  value = aws_iam_role.github_actions.arn
}

output "ssh_command" {
  value = "ssh -i ${local_sensitive_file.ec2_pem.filename} ubuntu@${aws_eip.api.public_ip}"
}

output "health_url" {
  value = "http://${aws_eip.api.public_ip}/actuator/health"
}

output "amplify_rewrite_target" {
  value = "http://${aws_eip.api.public_ip}/api/<*>"
}
