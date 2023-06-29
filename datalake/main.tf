terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }

  required_version = ">= 1.2.0"
}

provider "aws" {
  region  = "eu-west-2"
}

resource "aws_s3_bucket" "harmonized_buket" {
  bucket = "harmonized-bucket-datalake"

  tags = {
    Name = "HarmonizedBucketDatalake"
  }
}

