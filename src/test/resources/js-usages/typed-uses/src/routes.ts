import { OrderModel } from "./order";
import { User } from "./model";

export async function totals(): Promise<number> {
  const users = await User.findAll();
  return users.length + (await OrderModel.count());
}
