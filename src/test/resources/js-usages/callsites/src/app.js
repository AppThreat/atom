import gql from "graphql-tag";
import jwt from "jsonwebtoken";
import { Model } from "sequelize";
import { flatten } from "lodash";

export const Q = gql`
  query { a }
`;

export async function list() {
  const { S3Client } = await import("@aws-sdk/client-s3");
  const client = new S3Client({});
  return client.send(jwt.sign({ a: 1 }));
}

export class User extends Model {}

export const xs = [[1]].map(flatten);

export function identity() {
  return jwt;
}
