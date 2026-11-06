import { Model, type CreationOptional } from "sequelize";
import type { Request, Response } from "express";
import * as ng from "@angular/core";

export class User extends Model {
  declare id: CreationOptional<number>;

  save(): Promise<this> {
    return Promise.resolve(this);
  }
}

export async function list(req: Request, res: Response): Promise<void> {
  const users = await User.findAll();
  res.json(users);
}

export async function store(u: User): Promise<void> {
  await u.save();
  await u.reload();
}

interface Local {
  id: number;
}

export function local(l: Local): number {
  return l.id;
}

export const marker: ng.Type<unknown> | null = null;
