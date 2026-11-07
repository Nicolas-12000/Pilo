resource "aws_key_pair" "ec2" {
  key_name   = local.key_name
  public_key = tls_private_key.ec2.public_key_openssh
}

resource "tls_private_key" "ec2" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "local_sensitive_file" "ec2_pem" {
  content         = tls_private_key.ec2.private_key_pem
  filename        = "${path.module}/out/${local.key_name}.pem"
  file_permission = "0400"
}

resource "aws_eip" "api" {
  domain = "vpc"
  tags   = { Name = local.ec2_name }
}

resource "aws_instance" "api" {
  ami                    = data.aws_ami.ubuntu_arm.id
  instance_type          = "t4g.micro"
  key_name               = aws_key_pair.ec2.key_name
  vpc_security_group_ids = [aws_security_group.ec2.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2.name
  user_data              = file(var.ec2_user_data_path)

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  tags = { Name = local.ec2_name }

  lifecycle {
    ignore_changes = [ami]
  }
}

resource "aws_eip_association" "api" {
  instance_id   = aws_instance.api.id
  allocation_id = aws_eip.api.id
}
