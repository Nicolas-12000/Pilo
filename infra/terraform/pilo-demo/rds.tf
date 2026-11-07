resource "aws_db_subnet_group" "default" {
  name       = "pilo-default"
  subnet_ids = data.aws_subnets.default.ids
}

resource "aws_db_instance" "main" {
  identifier     = local.db_id
  engine         = "postgres"
  engine_version = "16.15"
  instance_class = "db.t4g.micro"

  allocated_storage     = 20
  storage_type          = "gp3"
  db_name               = "pilo"
  username              = "pilo_admin"
  password              = var.db_password
  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.default.name

  publicly_accessible    = false
  multi_az               = false
  backup_retention_period = 1
  storage_encrypted      = true
  deletion_protection    = false
  skip_final_snapshot    = true

  tags = {
    Name = local.db_id
  }
}
